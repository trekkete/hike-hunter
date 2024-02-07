package it.trekkete.hikehunter.data.entity;

public enum Equipment {
    RAMPONI(1),
    PICOZZA(2),
    CORDA(4),
    SCARPE_DA_ARRAMPICATA(8),
    CASCO(16),
    KIT_DA_FERRATA(32),
    IMBRAGO(64),
    CIASPOLE(128);

    final int flag;

    Equipment(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
