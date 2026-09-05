package com.github.adrianR_Souza.Barbearia.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigitoVerificador(cpf.substring(0, 9), new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2});
        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        int segundoDigito = calcularDigitoVerificador(cpf.substring(0, 10), new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2});
        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigitoVerificador(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
