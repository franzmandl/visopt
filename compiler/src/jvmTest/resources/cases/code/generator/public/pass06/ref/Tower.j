.source noSource
.class public Tower
.super java/lang/Object
.field private modifier I
.method public <init>()V
  aload_0
  invokespecial java/lang/Object/<init>()V
  return
.end method

.method public resetNumber()I
.limit stack 10
.limit locals 1
  ldc 2
  aload_0
  swap
  putfield Tower/modifier I
  ldc 0
  ireturn
.end method

.method public getNumber()I
.limit stack 10
.limit locals 1
  aload_0
  getfield Tower/modifier I
  ireturn
.end method

.method public multiply(I)I
.limit stack 10
.limit locals 3
  iload 1
  aload_0
  getfield Tower/modifier I
  imul
  istore 2
  aload_0
  getfield Tower/modifier I
  ldc 1
  iadd
  aload_0
  swap
  putfield Tower/modifier I
  iload 2
  ireturn
.end method

.method public divide(I)I
.limit stack 10
.limit locals 3
  iload 1
  aload_0
  getfield Tower/modifier I
  idiv
  istore 2
  aload_0
  getfield Tower/modifier I
  ldc 1
  iadd
  aload_0
  swap
  putfield Tower/modifier I
  iload 2
  ireturn
.end method

