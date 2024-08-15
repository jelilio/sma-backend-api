package io.github.jelilio.smbackend.common.utils;

public record Pair<R, T>(R r, T t) {

  public static <R, T> Pair<R, T> of(R r, T t) {
    return new Pair<>(r, t);
  }

  public R first() {
    return this.r;
  }

  public T second() {
    return this.t;
  }
}

