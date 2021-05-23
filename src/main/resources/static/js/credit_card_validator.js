function creditCardNumberValidator(card_number){

    let number_validity = document.getElementById("number_validity");
    const numberRegEx = /^[0-9]{16}$/;

    if(card_number.value.match(numberRegEx)){
        number_validity.innerHTML = '';
        return true;
    }
    else{
        number_validity.innerHTML = "Invalid Credit Card Number, 16 digits expected.";
        number_validity.style.color = "red";
        return false;
        }
}

function monthValidator(expiration_month){
    const month_validity = document.getElementById("month_validity");
    const monthRegex = /^[1-9]$|^[1][0-2]$/;

    if (expiration_month.value.match(monthRegex)){
        month_validity.innerHTML = "";
        return true;
    }
    else{
        month_validity.innerHTML = "Invalid expiration Month Selected, value 1 - 12 required.";
        month_validity.style.color = "red";
        return false;
    }

}

function yearValidator(expiration_year){
    const year_validity = document.getElementById("year_validity");
    const yearRegex = /^[2][0][2][1-9]$/;

    if (expiration_year.value.match(yearRegex)){
        year_validity.innerHTML = '';
        return true;
    }
    else{
        year_validity.innerHTML = 'Invalid Expiration Year Selected, year 2021 - 2029 required.';
        year_validity.style.colour = "red";
        return false;
    }

}

function cvvValidator(security_code) {
    const security_code_validity = document.getElementById("security_code_validity");
    const cvvRegex = /^[0-9]{3}$/;

    if (security_code.value.match(cvvRegex)){
        security_code_validity.innerHTML = '';
        return true;
        }
    else{
        security_code_validity.innerHTML = 'Incorrect CVV entered, 3-digit value required';
        security_code_validity.style.color = "red";
        return false;
    }
}

function clearCardErrors() {
    const errors = document.getElementsByClassName("error");

    for(let i = 0 ; i < errors.length ; i++) {
        errors[i].innerHTML = '';
    }
}