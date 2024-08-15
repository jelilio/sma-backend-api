package io.github.jelilio.smbackend.commonutil.utils;

public record Pair<R, T>(R left, T right) {
  public static <R, T> Pair<R, T> of(R r, T t) {
    return new Pair<>(r, t);
  }
}

