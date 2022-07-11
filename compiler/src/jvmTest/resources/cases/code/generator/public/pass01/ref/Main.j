.source noSource
.class public Main
.super java/lang/Object
.method public static main([Ljava/lang/String;)V
.limit stack 10
.limit locals 3
  new Evaluation
  dup
  invokespecial Evaluation/<init>()V
  astore 1
  aload 1
  invokevirtual Evaluation/init()I
  istore 2
  aload 1
  ldc "This course was so cool, I want to do it again next year!\n"
  ldc 716077
  invokevirtual Evaluation/setEvalMsg(Ljava/lang/String;I)I
  istore 2
  aload 1
  ldc "jk. Im glad its over..."
  ldc 716077
  invokevirtual Evaluation/setEvalMsg(Ljava/lang/String;I)I
  istore 2
  ldc 0
  invokestatic java/lang/System/exit(I)V
  return
.end method

