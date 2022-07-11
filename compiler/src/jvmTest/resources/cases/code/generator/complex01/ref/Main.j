.source noSource
.class public Main
.super java/lang/Object
.method public static main([Ljava/lang/String;)V
.limit stack 10
.limit locals 4
  new SecondClass
  dup
  invokespecial SecondClass/<init>()V
  astore 1
  new SecondClass
  dup
  invokespecial SecondClass/<init>()V
  astore 2
  ldc 1
  aload 1
  getfield SecondClass/fieldPublicFirstClass LFirstClass;
  swap
  putfield FirstClass/fieldPublicInt I
  aload 1
  getfield SecondClass/fieldPublicFirstClass LFirstClass;
  ldc 1
  aload 1
  getfield SecondClass/fieldPublicFirstClass LFirstClass;
  invokevirtual FirstClass/methodPublicInt(ZLFirstClass;)I
  istore 3
  ldc 0
  invokestatic java/lang/System/exit(I)V
  return
.end method

