package com.vilaverde.erp.service;
import com.vilaverde.erp.exception.InvalidInputException;
import java.util.Locale;
public final class Documents {
  private Documents() {}
  public static String normalize(String value) {
    return value==null ? "" : value.strip().toUpperCase(Locale.ROOT).replaceAll("[. /\\-]", "");
  }
  public static boolean valid(String value, boolean cnpj) {
    String v=normalize(value);int size=cnpj?14:11;
    if(!v.matches(cnpj?"[A-Z0-9]{12}[0-9]{2}":"[0-9]{11}") || v.chars().distinct().count()==1)return false;
    for(int n=size-2;n<size;n++) {
      int sum=0;
      for(int i=0;i<n;i++)sum+=(v.charAt(i)-48)*(cnpj?((n-1-i)%8+2):(n+1-i));
      int digit=11-sum%11;if(digit>=10)digit=0;
      if(v.charAt(n)-48!=digit)return false;
    }
    return true;
  }
  public static String parse(String value, boolean cnpj, boolean optional) {
    String v=normalize(value);if(optional && v.isEmpty())return null;
    if(!valid(v,cnpj))throw new InvalidInputException(cnpj?"CNPJ inválido":"CPF inválido");return v;
  }
}
