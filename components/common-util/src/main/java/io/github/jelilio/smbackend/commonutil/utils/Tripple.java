package io.github.jelilio.smbackend.commonutil.utils;

public record Tripple<L, M, R>(L left, M middle, R right) {
  public static <L, M, R> Tripple<L, M, R> of(L l, M m, R r) {
    return new Tripple<>(l, m, r);
  }
}

