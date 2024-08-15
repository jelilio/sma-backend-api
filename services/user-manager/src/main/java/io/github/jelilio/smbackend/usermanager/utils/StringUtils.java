package io.github.jelilio.smbackend.usermanager.utils;

import io.github.jelilio.smbackend.common.utils.Pair;

import java.util.Arrays;

public final class StringUtils {
  public static Pair<String, String> splitName(String name) {
    var names = name.split("\\s");
    var first = names[0];
    var others = names.length>1? String.join(" ", Arrays.copyOfRange(names, 1, names.length)) : null;
    return new Pair<>(first, others);
  }
}
