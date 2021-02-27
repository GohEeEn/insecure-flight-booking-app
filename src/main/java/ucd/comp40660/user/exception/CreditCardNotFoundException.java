package ucd.comp40660.user.exception;

public class CreditCardNotFoundException extends Exception{
    private Long id;

    public CreditCardNotFoundException(long id){
        super(String.format("Credit card with id '%s' not found.", id));
    }
}
