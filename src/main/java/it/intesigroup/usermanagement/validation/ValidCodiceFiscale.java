package it.intesigroup.usermanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotazione di validazione per il codice fiscale italiano.
 *
 * <p>Applicabile a campi di tipo {@link String}. La logica di validazione
 * è implementata in {@link CodiceFiscaleValidator}.
 *
 * <p>Può essere applicata anche a campi null — in quel caso la validazione
 * viene saltata. Per rendere il campo obbligatorio combinare con {@code @NotBlank}.
 */
@Documented
@Constraint(validatedBy = CodiceFiscaleValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCodiceFiscale {

    String message() default "Codice fiscale non valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
