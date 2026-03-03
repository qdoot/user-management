package it.intesigroup.usermanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementazione della validazione del codice fiscale italiano.
 *
 * <p>Verifica:
 * <ol>
 *   <li>Formato tramite regex</li>
 *   <li>Cifra di controllo calcolata secondo l'algoritmo ministeriale</li>
 * </ol>
 *
 * <p>Fonte algoritmo: DM 23/12/1976, Ministero delle Finanze italiano.
 */
public class CodiceFiscaleValidator
        implements ConstraintValidator<ValidCodiceFiscale, String> {

    private static final String CF_REGEX =
            "^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$";

    /**
     * Valori dei caratteri in posizione dispari (1-based, 0-indexed pari).
     * Fonte: tabella ministeriale allegato B del DM 23/12/1976.
     */
    private static final int[] ODD_VALUES = {
            1, 0, 5, 7, 9, 13, 15, 17, 19, 21,
            2, 4, 18, 20, 11, 3, 6, 8, 12, 14,
            16, 10, 22, 25, 24, 23
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String cf = value.toUpperCase().trim();

        if (!cf.matches(CF_REGEX)) {
            return false;
        }

        return isControlCharValid(cf);
    }

    /**
     * Calcola e verifica la cifra di controllo secondo l'algoritmo ministeriale.
     */
    private boolean isControlCharValid(String cf) {
        int sum = 0;

        for (int i = 0; i < 15; i++) {
            char c = cf.charAt(i);
            int value;

            if (i % 2 == 0) {
                // Posizione dispari (0-indexed pari): tabella ODD_VALUES
                value = ODD_VALUES[Character.isDigit(c) ? (c - '0') : (c - 'A')];
            } else {
                // Posizione pari (0-indexed dispari): valore diretto
                value = Character.isDigit(c) ? (c - '0') : (c - 'A');
            }

            sum += value;
        }

        char expectedControl = (char) ('A' + (sum % 26));
        return cf.charAt(15) == expectedControl;
    }
}
