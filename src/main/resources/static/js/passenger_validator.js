function passengerForenameValidator(name){

    let forename_validity = document.getElementById("forename_validity");
    const nameRegex = /^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/;

    if(name.value.match(nameRegex)){
        forename_validity.innerHTML = "";
        return true;
    }
    else{
        forename_validity.innerHTML = "Invalid Name Given.";
        forename_validity.style.color = "red";
        return false;
    }
}

function passengerSurnameValidator(name){

    let surname_validity = document.getElementById("surname_validity");
    const nameRegex = /^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/

    if(name.value.match(nameRegex)){
        surname_validity.innerHTML = "";
        return true;
    }
    else{
        surname_validity.innerHTML = "Invalid Surname Given.";
        surname_validity.style.color = "red";
        return false;
    }
}


function passengerAddressValidator(address){

    let address_validity = document.getElementById("address_validity");
    const addressRegex = /^[a-zA-Z0-9]+(([',. -][a-zA-Z0-9 ])?[a-zA-Z0-9 ]*)*$/;

    if(address.value.match(addressRegex)){
        address_validity.innerHTML = "";
        return true;
    }
    else{
        address_validity.innerHTML = "Invalid Address Given.";
        address_validity.style.color = "red";
        return false;
    }
}

function passengerEmailValidator(email){

    let email_validity = document.getElementById("email_validity");
    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/

    if(email.value.match(emailRegex)){
        email_validity.innerHTML = "";
        return true;
    }
    else{
        email_validity.innerHTML = "Invalid E-mail Given.";
        email_validity.style.color = "red";
        return false;
    }
}

function passengerPhoneValidator(phone){

    let phone_validity = document.getElementById("phone_validity");
    const phoneRegex = /^[0-9]{3}[-][0-9]{3}[-][0-9]{4}$/

    if(phone.value.match(phoneRegex)){
        phone_validity.innerHTML = "";
        return true;
    }
    else{
        phone_validity.innerHTML = "Invalid Phone Number Given.";
        phone_validity.style.color = "red";
        return false;
    }
}

function clearPassengerErrors(){
    const errors = document.getElementsByClassName("error");

    for(let i = 0 ; i < errors.length ; i++) {
        errors[i].innerHTML = '';
    }
}

