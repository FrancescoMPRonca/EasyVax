package com.easyvax.exception.enums;

public enum PersonaleEnum {

    PERSONALE_NOT_FOUND("PERS_NF", "Il personale che stai cercando non è stato trovato"),
    PERSONALE_ALREADY_EXISTS("PERS_AE", "Il personale che vuoi inserire è già presente nel sistema"),
    PERSONALE_DELETE_ERROR("PERS_DLE", "Errore durante l'eliminazione del personale"),
    PERSONALE_ID_NOT_EXIST("PERS_IDNE", "Il personale che stai cercando non esiste"),
    PERSONALE_NOT_EXIST("PERS_NE","Non c'è alcun personale inserito nel DB per il centro vaccinale selezionato"),
    PERSONALE_EMPTY_FIELD("PERS_EF", "Alcuni campi sono vuoti");

    private final String messageCode;
    private final String message;

    PersonaleEnum(final String messageCode, final String message) {
        this.messageCode = messageCode;
        this.message = message;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getMessage() {
        return message;
    }

    public static PersonaleEnum getPersonaleEnumByMessageCode(final String messageCode) {
        for (final PersonaleEnum personaleEnum : PersonaleEnum.values()) {
            if (personaleEnum.getMessageCode().equals(messageCode)) {
                return personaleEnum;
            }
        }
        return null;
    }
}
