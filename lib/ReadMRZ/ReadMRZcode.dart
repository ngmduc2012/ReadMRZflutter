class readMRZ {
  String documentType(String textMRZcode) {
    return textMRZcode.substring(0, 2);
  }

  String countryCode(String textMRZcode) {
    return textMRZcode.substring(2, 5);
  }

  String documentNumber(String textMRZcode) {
    return textMRZcode.substring(15, 27);
  }

  String documentNumberCheckDigit(String textMRZcode) {
    return textMRZcode.substring(14, 15);
  }

  String optionalData(String textMRZcode) {
    return textMRZcode.substring(15, 30);
  }

  String birthDate(String textMRZcode) {
    return textMRZcode.substring(30, 36);
  }
  String showBirthDate(String textMRZcode){
    String yeas = "20";
    if(int.parse(textMRZcode.substring(30,32)) > DateTime.now().year % 100){
      yeas = "19";
    }
    return textMRZcode.substring(34,36) + "/" + textMRZcode.substring(32,34) + "/" + yeas + textMRZcode.substring(30,32);
  }

  String birthDateCheckDigit(String textMRZcode) {
    return textMRZcode.substring(36, 37);
  }

  String sex(String textMRZcode) {
    if(textMRZcode.substring(37, 38) == "F")
      {
        return "Female";
      }
    return "Male";
    // return textMRZcode.substring(37, 38);
  }

  String expiryDate(String textMRZcode) {
    return textMRZcode.substring(38, 44);
  }
  String showExpiryDate(String textMRZcode){
    if(textMRZcode.substring(42,44) + "/" + textMRZcode.substring(40,42) + "/${(DateTime.now().year/100).toInt()}"  + textMRZcode.substring(38,40) ==  "31/12/2099"){
      return "Không thời hạn";
    }
    return textMRZcode.substring(42,44) + "/" + textMRZcode.substring(40,42) + "/${(DateTime.now().year/100).toInt()}"  + textMRZcode.substring(38,40);
  }

  String expiryDateCheckDigit(String textMRZcode) {
    return textMRZcode.substring(44, 45);
  }

  String nationality(String textMRZcode) {
    return textMRZcode.substring(45, 48);
  }

  String optionalData2(String textMRZcode) {
    return textMRZcode.substring(48, 59);
  }

  String finalCheckDigit(String textMRZcode) {
    return textMRZcode.substring(59, 60);
  }

  String names(String textMRZcode) {
    return textMRZcode.substring(60, 90).replaceAll("<", " ").trim().replaceAll("  ", " ");
  }
}
