package io.github.jelilio.smbackend.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ValidatorUtil {
  private static final String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

  public static boolean checkIfValid(String email){
    Pattern pattern = Pattern.compile(EMAIL_REGEX);
    Matcher mat = pattern.matcher(email);

    return mat.matches();
  }
}
