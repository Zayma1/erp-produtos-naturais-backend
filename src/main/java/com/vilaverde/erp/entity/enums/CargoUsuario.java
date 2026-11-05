package com.vilaverde.erp.entity.enums;

public enum CargoUsuario {
    ADMIN("admin"),
    FUNC("funcionario"),
    GER("gerente");

    private String cargo;

    CargoUsuario(String cargo){
        this.cargo = cargo;
    }
}
