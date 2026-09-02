package com.github.adrianR_Souza.Barbearia.Security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    void permiteConsumirAteACapacidadeEDepoisBarra() {
        TokenBucket balde = new TokenBucket(3, 0.0001);

        assertThat(balde.tentarConsumir()).isTrue();
        assertThat(balde.tentarConsumir()).isTrue();
        assertThat(balde.tentarConsumir()).isTrue();
        assertThat(balde.tentarConsumir()).isFalse();
    }

    @Test
    void recarregaTokensComOTempo() throws InterruptedException {
        TokenBucket balde = new TokenBucket(1, 100);

        assertThat(balde.tentarConsumir()).isTrue();
        assertThat(balde.tentarConsumir()).isFalse();

        Thread.sleep(50);

        assertThat(balde.tentarConsumir()).isTrue();
    }

    @Test
    void naoUltrapassaACapacidadeMaximaMesmoDepoisDeMuitoTempoOcioso() throws InterruptedException {
        TokenBucket balde = new TokenBucket(2, 1000);

        Thread.sleep(20);

        assertThat(balde.tentarConsumir()).isTrue();
        assertThat(balde.tentarConsumir()).isTrue();
        assertThat(balde.tentarConsumir()).isFalse();
    }
}
