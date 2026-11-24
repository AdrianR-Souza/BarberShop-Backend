package com.github.adrianR_Souza.Barbearia.Validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void aceitaCpfValido() {
        assertThat(validator.isValid("11144477735", null)).isTrue();
    }

    @Test
    void rejeitaCpfComDigitoVerificadorErrado() {
        assertThat(validator.isValid("11144477736", null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000", "11111111111", "22222222222", "99999999999"
    })
    void rejeitaCpfComTodosOsDigitosIguais(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @Test
    void rejeitaCpfComMenosDe11Digitos() {
        assertThat(validator.isValid("1234567890", null)).isFalse();
    }

    @Test
    void rejeitaCpfComCaracteresNaoNumericos() {
        assertThat(validator.isValid("111.444.777-35", null)).isFalse();
    }

    @Test
    void rejeitaNulo() {
        assertThat(validator.isValid(null, null)).isFalse();
    }
}
