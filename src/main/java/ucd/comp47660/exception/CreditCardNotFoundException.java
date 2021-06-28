package ucd.comp47660.exception;

public class CreditCardNotFoundException extends Exception{
    private Long id;

    public CreditCardNotFoundException(long id){
        super(String.format("Credit card with id '%s' not found.", id));
    }
}
