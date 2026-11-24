package com.github.adrianR_Souza.Barbearia.Security;

class TokenBucket {

    private final double capacidade;
    private final double tokensPorSegundo;
    private double tokensDisponiveis;
    private long ultimoRefil;
    private volatile long ultimoAcesso;

    TokenBucket(double capacidade, double tokensPorSegundo) {
        this.capacidade = capacidade;
        this.tokensPorSegundo = tokensPorSegundo;
        this.tokensDisponiveis = capacidade;
        this.ultimoRefil = System.nanoTime();
        this.ultimoAcesso = System.currentTimeMillis();
    }

    synchronized boolean tentarConsumir() {
        recarregar();
        ultimoAcesso = System.currentTimeMillis();

        if (tokensDisponiveis >= 1) {
            tokensDisponiveis -= 1;
            return true;
        }
        return false;
    }

    long millisDesdeUltimoAcesso() {
        return System.currentTimeMillis() - ultimoAcesso;
    }

    private void recarregar() {
        long agora = System.nanoTime();
        double segundosPassados = (agora - ultimoRefil) / 1_000_000_000.0;
        tokensDisponiveis = Math.min(capacidade, tokensDisponiveis + segundosPassados * tokensPorSegundo);
        ultimoRefil = agora;
    }
}
