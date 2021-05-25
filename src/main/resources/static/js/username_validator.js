function checkUsername(username){

    let username_validity = document.getElementById("username_validity");
    const usernameRegex = /^[A-Za-z0-9_]{4,20}$/;

    if(username.value.match(usernameRegex)){
        username_validity.innerHTML = "";
        return true;
    }
    else{
        username_validity.innerHTML = "Invalid username, entry expected to be 4-20 alphanumeric characters, including underscore.";
        username_validity.style.color = "red";
        return false;
    }
}