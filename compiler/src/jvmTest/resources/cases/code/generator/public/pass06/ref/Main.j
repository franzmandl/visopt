.source noSource
.class public Main
.super java/lang/Object
.method public static main([Ljava/lang/String;)V
.limit stack 10
.limit locals 5
  new Tower
  dup
  invokespecial Tower/<init>()V
  astore 1
  new Printer
  dup
  invokespecial Printer/<init>()V
  astore 2
  ldc 42
  istore 3
  aload 2
  iload 3
  invokevirtual Printer/printf(I)I
  istore 4
  aload 1
  invokevirtual Tower/resetNumber()I
  istore 4
  L_0:
  aload 1
  invokevirtual Tower/getNumber()I
  ldc 10
  if_icmplt L_2
  ldc 0
  goto L_3
  L_2:
  ldc 1
  L_3:
  ifeq L_1
  aload 1
  iload 3
  invokevirtual Tower/multiply(I)I
  istore 3
  aload 2
  iload 3
  invokevirtual Printer/printf(I)I
  istore 4
  goto L_0
  L_1:
  aload 1
  invokevirtual Tower/resetNumber()I
  istore 4
  L_4:
  aload 1
  invokevirtual Tower/getNumber()I
  ldc 10
  if_icmplt L_6
  ldc 0
  goto L_7
  L_6:
  ldc 1
  L_7:
  ifeq L_5
  aload 1
  iload 3
  invokevirtual Tower/divide(I)I
  istore 3
  aload 2
  iload 3
  invokevirtual Printer/printf(I)I
  istore 4
  goto L_4
  L_5:
  ldc 0
  invokestatic java/lang/System/exit(I)V
  return
.end method

