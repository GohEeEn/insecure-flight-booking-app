function CheckPasswordStrength(password) {

    const password_strength = document.getElementById("password_strength");

    // TextBox left blank.
    if (password.length === 0) {
        password_strength.innerHTML = "";
        return;
    }

    // Regular Expressions.
    const regex = [];
    regex.push("[a-z]");        // Lowercase Alphabet.
    regex.push("[A-Z]");        // Uppercase Alphabet.
    regex.push("[0-9]");        // Digit.
    regex.push("[$@$!%*#?&]");  // Special Character.

    /*
    * (?=.*[a-z])   The string must contain at least 1 lowercase alphabetical character
    * (?=.*[A-Z])	The string must contain at least 1 uppercase alphabetical character
    * (?=.*[0-9])	The string must contain at least 1 numeric character
    * (?=.*[!@#$%^&*])	The string must contain at least one special character, but we are escaping reserved RegEx characters to avoid conflict
    * (?=.{8,})	The string must be eight characters or longer
    */
    let passed = 0;

    // Validate for each Regular Expression.
    for (const item of regex) {
        if (new RegExp(item).test(password)) {
            passed++;
        }
    }

    // Validate for length of Password.
    if (passed > 2 && password.length >= 8) {
        passed++;
    }

    //Display status.
    let color = "";
    let strength = "";
    switch (passed) {
        case 0:
        case 1:
            strength = "Very Weak";
            color = "darkred";
            break;
        case 2:
            strength = "Weak";
            color = "red";
            break;
        case 3:
            strength = "Fair";
            color = "darkorange";
            break;
        case 4:
            strength = "Strong";
            color = "green";
            break;
        case 5:
            strength = "Very Strong";
            color = "darkgreen";
            break;
    }

    password_strength.innerHTML = strength;
    password_strength.style.color = color;
}

function clearErrors() {
    var errors = document.getElementsByClassName("error");

    for(let i = 0 ; i < errors.length ; i++) {
        errors[i].innerHTML = '';
    }
}