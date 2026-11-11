package db.migration;
import org.flywaydb.core.api.migration.*;
import java.sql.*;
import java.util.*;
import com.vilaverde.erp.service.Documents;
import com.vilaverde.erp.service.AuthorizationService;
/** No network access. All data conflicts are reported before any DDL/write. */
public class V9__forms_auth extends BaseJavaMigration {
  @Override public Integer getChecksum(){return 14001;}
  @Override public void migrate(Context context) throws Exception {
    var c=context.getConnection();var problems=new ArrayList<String>();
    for(String table:List.of("cliente","fornecedor","loja")) {
      var seen=new HashMap<String,String>();
      try(var st=c.createStatement();var rs=st.executeQuery("select id,"+(table.equals("loja")?"cnpj":table.equals("cliente")?"cpf":"cpf,cnpj")+" from "+table+" order by id")) {
        while(rs.next()) {
          String id=rs.getString("id");
          if(table.equals("fornecedor") && Documents.normalize(rs.getString("cpf")).isEmpty()==Documents.normalize(rs.getString("cnpj")).isEmpty())problems.add(table+" "+id+": informe exatamente um documento");
          for(String field:table.equals("loja")?List.of("cnpj"):table.equals("cliente")?List.of("cpf"):List.of("cpf","cnpj")) {
            String v=Documents.normalize(rs.getString(field));
            if(v.isEmpty() && !table.equals("loja"))continue;
            if(!Documents.valid(v,field.equals("cnpj")))problems.add(table+" "+id+": "+field+" inválido");
            String other=seen.putIfAbsent(field+":"+v,id);
            if(other!=null)problems.add(table+" "+field+": duplicado IDs "+other+", "+id);
          }
        }
      }
    }
    try(var st=c.createStatement();var rs=st.executeQuery("select lower(regexp_replace(btrim(nome),'[[:space:]]+',' ','g')) nome,string_agg(id::text,', ' order by id) ids from unidade_medida group by 1 having count(*)>1")) {
      while(rs.next())problems.add("unidade_medida: "+rs.getString("ids"));
    }
    if(!problems.isEmpty())throw new SQLException("V9 preflight: corrija explicitamente os registros antes de migrar:\n"+String.join("\n",problems));
    try(var st=c.createStatement()) {
      st.execute("alter table usuario add column usuario varchar(32)");
      st.execute("create table usuario_login_migracao(id_usuario uuid primary key references usuario(id),usuario varchar(32) not null)");
    }
    Set<String> used=new HashSet<>();used.add("admin");
    try(var st=c.createStatement();var rs=st.executeQuery("select id,email from usuario order by case when email='admin@vilaverde.local' then 0 else 1 end,id")) {
      while(rs.next()) {
        String id=rs.getString(1),email=rs.getString(2),base=AuthorizationService.suggest(email),name=base;
        if(email.equals(AuthorizationService.DEFAULT_ADMIN_EMAIL))name="admin";
        else if(used.contains(name)) {
          String suffix=id.replace("-","");int n=8;
          do {String tail="-"+suffix.substring(0,n++);name=base.substring(0,Math.min(base.length(),32-tail.length()))+tail;}while(used.contains(name));
        }
        used.add(name);
        try(var update=c.prepareStatement("update usuario set usuario=? where id=?::uuid")) {update.setString(1,name);update.setString(2,id);update.executeUpdate();}
        try(var update=c.prepareStatement("insert into usuario_login_migracao values (?::uuid,?)")) {update.setString(1,id);update.setString(2,name);update.executeUpdate();}
      }
    }
    try(var st=c.createStatement()) {
      st.execute("alter table usuario alter column usuario set not null");
      st.execute("alter table usuario add constraint usuario_login_format check (usuario ~ '^[a-zA-Z0-9._-]{3,32}$')");
      st.execute("create unique index usuario_login_unique on usuario(lower(usuario))");
      // Existing sessions predate the identity contract.
      st.execute("delete from spring_session");
      st.execute("create table password_reset(token_hash char(64) primary key,usuario_id uuid not null references usuario(id),expires_at timestamptz not null)");
      st.execute("create index password_reset_user on password_reset(usuario_id)");
      st.execute("create table password_reset_attempt(identifier char(64) primary key,started_at timestamptz not null,requests integer not null)");
      for(String table:List.of("loja","fornecedor"))st.execute("alter table "+table+" add column municipio_ibge integer");
      for(String table:List.of("cliente","fornecedor","loja")) {
        for(String field:table.equals("loja")?List.of("cnpj"):table.equals("cliente")?List.of("cpf"):List.of("cpf","cnpj")) {
          st.execute("update "+table+" set "+field+"=nullif(upper(regexp_replace(btrim("+field+"),'[. /-]','','g')),'')");
          st.execute("create unique index "+table+"_"+field+"_unique on "+table+"("+field+") where "+field+" is not null");
        }
      }
      st.execute("update unidade_medida set nome=regexp_replace(btrim(nome),'[[:space:]]+',' ','g')");
      st.execute("create unique index unidade_nome_unique on unidade_medida(lower(regexp_replace(btrim(nome),'[[:space:]]+',' ','g')))");
    }
  }
}
