// prettier-ignore
declare module 'compiler-generated' {

    export interface Body {
        readonly arguments: ReadonlyArray<Variable>;
        readonly compound: Compound;
        readonly cfg: Cfg;
        readonly bodyInfo: BodyInfo;
    }

    export interface BodyInfo {
        readonly coercionToBoolCounter?: number;
        readonly coercionToAnyCounter?: number;
        readonly basicBlockCounter?: number;
        readonly logicalAndCounter?: number;
        readonly logicalOrCounter?: number;
        readonly logicalNotCounter?: number;
        readonly relationalCounter?: number;
        readonly objectEqualsCounter?: number;
        readonly ternaryCounter?: number;
        readonly temporaryVariableCounter?: number;
    }

    export interface BodyInfoChange {
        readonly oldInfo: BodyInfo;
        readonly newInfo: BodyInfo;
    }

    export interface Cfg {
        readonly list: ReadonlyArray<CfgNode>;
    }

    export interface CfgNode {
        readonly id: number;
        readonly naturalPredecessor: number | null;
        readonly complexPredecessors: ReadonlyArray<number>;
        readonly naturalSuccessor: number | null;
        readonly complexSuccessor: number | null;
        readonly selfSuccessor: boolean;
        readonly inverted: boolean;
    }

    export interface Clazz {
        readonly id: string;
        readonly symbols: ReadonlyArray<ClassSymbol>;
    }

    export interface Compound {
        readonly statements: ReadonlyArray<CompoundStatement>;
    }

    export interface ConstructorSignature {
        readonly isDefault: boolean;
        readonly signature: Signature;
    }

    export interface ExpressionBlock {
        readonly basicBlock: BasicBlock;
        readonly expression: Expression;
    }

    export interface JasminSignature {
        readonly name: string;
        readonly argumentTypes: ReadonlyArray<JasminValueType>;
        readonly returnType: JasminType;
    }

    export interface Location {
        readonly line: number;
        readonly position: number;
    }

    export interface LoopMode {
        readonly endInclusive: number;
        readonly step: number;
    }

    export interface MappingEntry {
        readonly variable: Variable;
        readonly expression: Expression | null;
    }

    export interface MethodSignature {
        readonly accessModifier: AccessModifier;
        readonly isMain: boolean;
        readonly signature: Signature;
        readonly returnType: string;
    }

    export interface Program {
        readonly fileName: string;
        readonly needsScanner: boolean;
        readonly classes: ReadonlyArray<Clazz>;
    }

    export interface Signature {
        readonly name: string;
        readonly argumentTypes: ReadonlyArray<string>;
    }

    export interface SignatureNullable {
        readonly name: string;
        readonly argumentTypes: ReadonlyArray<string>;
    }

    export interface Variable {
        readonly id: string;
        readonly level: number | null;
        readonly type: string;
    }

    export type AccessModifier = 'private' | 'public';

    export type ArithmeticBinaryOperator = '-' | '%' | '+' | '<<' | '>>' | '/' | '*';

    export type ArithmeticUnaryOperator = '-' | '+';

    export type LogicalBinaryOperator = '&&' | '||';

    export type ObjectEqualsBinaryOperator = '==' | '!=';

    export type Optimization = 'AlgebraicSimplifications' | 'CommonSubexpressionElimination' | 'ConstantFolding' | 'ConstantPropagation' | 'CopyPropagation' | 'DeadCodeElimination' | 'ReductionInStrength' | 'ThreeAddressCode';

    export type Phase = 'Lexer' | 'Parser' | 'TypeChecker';

    export type RelationalBinaryOperator = '==' | '!=' | '>=' | '>' | '<=' | '<';

    export type Address =
        | {readonly discriminator: 'ProgramAddress'}
        | ({readonly discriminator: 'BodyAddress'} & BodyAddress)
        | ({readonly discriminator: 'CompoundAddress'} & CompoundAddress)
        | ({readonly discriminator: 'CompoundStatementAddress'} & CompoundStatementAddress)
        | ({readonly discriminator: 'BasicBlockAddress'} & BasicBlockAddress)
        | ({readonly discriminator: 'ExpressionBlockAddress'} & ExpressionBlockAddress)
        | ({readonly discriminator: 'BasicStatementAddress'} & BasicStatementAddress)
        | ({readonly discriminator: 'ExpressionAddress'} & ExpressionAddress);

    export interface BodyAddress {
        readonly classId: string;
        readonly signature: Signature;
    }

    export interface CompoundAddress {
        readonly bodyAddress: BodyAddress;
        readonly indices: ReadonlyArray<number>;
    }

    export interface CompoundStatementAddress {
        readonly compoundAddress: CompoundAddress;
        readonly index: number;
    }

    export interface BasicBlockAddress {
        readonly compoundStatementAddress: CompoundStatementAddress;
    }

    export interface ExpressionBlockAddress {
        readonly compoundStatementAddress: CompoundStatementAddress;
    }

    export interface BasicStatementAddress {
        readonly basicBlockAddress: BasicBlockAddress;
        readonly index: number;
    }

    export interface ExpressionAddress {
        readonly basicStatementAddress: BasicStatementAddress;
        readonly rootIndex: number;
        readonly indices: ReadonlyArray<number>;
    }

    export type BasicStatement =
        | ({readonly discriminator: 'Assignment'} & Assignment)
        | ({readonly discriminator: 'ExpressionStatement'} & ExpressionStatement)
        | ({readonly discriminator: 'VariableDeclarations'} & VariableDeclarations);

    export interface Assignment {
        readonly lhs: AssignableExpression;
        readonly rhs: Expression;
    }

    export interface ExpressionStatement {
        readonly expression: Expression;
    }

    export interface VariableDeclarations {
        readonly type: string;
        readonly variables: ReadonlyArray<Variable>;
    }

    export type BuiltinMethod =
        | BuiltinPrintMethod
        | BuiltinReadMethod;

    export type BuiltinPrintMethod =
        | {readonly discriminator: 'PrintBoolMethod'}
        | {readonly discriminator: 'PrintIntMethod'}
        | {readonly discriminator: 'PrintStringMethod'};

    export type BuiltinReadMethod =
        | {readonly discriminator: 'ReadIntMethod'}
        | {readonly discriminator: 'ReadStringMethod'};

    export type ClassSymbol =
        | HasBodySymbol
        | ({readonly discriminator: 'Member'} & Member);

    export type HasBodySymbol =
        | ({readonly discriminator: 'Constructor'} & Constructor)
        | ({readonly discriminator: 'Method'} & Method);

    export interface Constructor {
        readonly constructorSignature: ConstructorSignature;
        readonly body: Body;
    }

    export interface Method {
        readonly methodSignature: MethodSignature;
        readonly body: Body;
    }

    export interface Member {
        readonly accessModifier: AccessModifier;
        readonly id: string;
        readonly type: string;
    }

    export type Command =
        | ({readonly discriminator: 'AddBasicStatement'} & AddBasicStatement)
        | ({readonly discriminator: 'RemoveBasicStatement'} & RemoveBasicStatement)
        | ({readonly discriminator: 'ReplaceBasicStatement'} & ReplaceBasicStatement)
        | ({readonly discriminator: 'ReplaceCompoundStatement'} & ReplaceCompoundStatement)
        | ({readonly discriminator: 'ReplaceExpression'} & ReplaceExpression)
        | ({readonly discriminator: 'TakeBranch'} & TakeBranch);

    export interface AddBasicStatement {
        readonly optimization: Optimization;
        readonly bodyInfoChange: BodyInfoChange | null;
        readonly address: BasicStatementAddress;
        readonly toAdd: BasicStatement;
    }

    export interface RemoveBasicStatement {
        readonly optimization: Optimization;
        readonly bodyInfoChange: BodyInfoChange | null;
        readonly address: BasicStatementAddress;
        readonly toRemove: BasicStatement;
        readonly liveVariables: ReadonlyArray<Variable>;
    }

    export interface ReplaceBasicStatement {
        readonly optimization: Optimization;
        readonly bodyInfoChange: BodyInfoChange | null;
        readonly address: BasicStatementAddress;
        readonly old: BasicStatement;
        readonly replacement: BasicStatement;
        readonly liveVariables: ReadonlyArray<Variable>;
    }

    export interface ReplaceCompoundStatement {
        readonly optimization: Optimization;
        readonly bodyInfoChange: BodyInfoChange | null;
        readonly address: CompoundStatementAddress;
        readonly replacement: BasicBlock;
        readonly reason: string;
    }

    export interface ReplaceExpression {
        readonly optimization: Optimization;
        readonly bodyInfoChange: BodyInfoChange | null;
        readonly address: ExpressionAddress;
        readonly old: Expression;
        readonly replacement: Expression;
        readonly reason: ReplaceExpressionReason;
        readonly addStatement: AddBasicStatement | null;
    }

    export interface TakeBranch {
        readonly optimization: Optimization;
        readonly bodyInfoChange: BodyInfoChange | null;
        readonly address: CompoundStatementAddress;
        readonly condition: BasicBlock;
        readonly takenBranch: Compound;
        readonly reason: string;
    }

    export type CompoundStatement =
        | ({readonly discriminator: 'BasicBlock'} & BasicBlock)
        | ControlStatement;

    export interface BasicBlock {
        readonly id: number;
        readonly statements: ReadonlyArray<BasicStatement>;
    }

    export type ControlStatement =
        | ({readonly discriminator: 'IfStatement'} & IfStatement)
        | ({readonly discriminator: 'ReturnStatement'} & ReturnStatement)
        | ({readonly discriminator: 'WhileStatement'} & WhileStatement);

    export interface IfStatement {
        readonly expressionBlock: ExpressionBlock;
        readonly thenBranch: Compound;
        readonly elseBranch: Compound | null;
        readonly id: number;
    }

    export interface ReturnStatement {
        readonly expressionBlock: ExpressionBlock;
        readonly id: number;
    }

    export interface WhileStatement {
        readonly expressionBlock: ExpressionBlock;
        readonly branch: Compound;
        readonly id: number;
    }

    export type Expression =
        | ({readonly discriminator: 'ArithmeticUnaryOperation'} & ArithmeticUnaryOperation)
        | ({readonly discriminator: 'LogicalNotUnaryOperation'} & LogicalNotUnaryOperation)
        | BinaryOperation
        | ({readonly discriminator: 'TernaryOperation'} & TernaryOperation)
        | ({readonly discriminator: 'CoercionExpression'} & CoercionExpression)
        | LiteralExpression
        | AssignableExpression
        | InvocationExpression;

    export interface ArithmeticUnaryOperation {
        readonly operator: ArithmeticUnaryOperator;
        readonly operand: Expression;
        readonly needsBraces: boolean;
    }

    export interface LogicalNotUnaryOperation {
        readonly id: number;
        readonly operand: Expression;
        readonly needsBraces: boolean;
    }

    export type BinaryOperation =
        | ({readonly discriminator: 'ArithmeticBinaryOperation'} & ArithmeticBinaryOperation)
        | ({readonly discriminator: 'LogicalBinaryOperation'} & LogicalBinaryOperation)
        | ({readonly discriminator: 'RelationalBinaryOperation'} & RelationalBinaryOperation)
        | ({readonly discriminator: 'ObjectEqualsBinaryOperation'} & ObjectEqualsBinaryOperation);

    export interface ArithmeticBinaryOperation {
        readonly operator: ArithmeticBinaryOperator;
        readonly operands: BinaryOperands;
        readonly needsLhsBraces: boolean;
        readonly needsRhsBraces: boolean;
    }

    export interface LogicalBinaryOperation {
        readonly id: number;
        readonly operator: LogicalBinaryOperator;
        readonly operands: BinaryOperands;
        readonly needsLhsBraces: boolean;
        readonly needsRhsBraces: boolean;
    }

    export interface RelationalBinaryOperation {
        readonly id: number;
        readonly operator: RelationalBinaryOperator;
        readonly operands: BinaryOperands;
        readonly needsLhsBraces: boolean;
        readonly needsRhsBraces: boolean;
    }

    export interface ObjectEqualsBinaryOperation {
        readonly id: number;
        readonly operator: ObjectEqualsBinaryOperator;
        readonly operands: BinaryOperands;
        readonly needsLhsBraces: boolean;
        readonly needsRhsBraces: boolean;
    }

    export interface TernaryOperation {
        readonly id: number;
        readonly condition: Expression;
        readonly operands: BinaryOperands;
        readonly needsConditionBraces: boolean;
        readonly needsLhsBraces: boolean;
        readonly needsRhsBraces: boolean;
    }

    export interface CoercionExpression {
        readonly id: number;
        readonly operand: Expression;
        readonly expectedType: string;
        readonly acceptedType: string;
    }

    export type LiteralExpression =
        | BooleanLiteral
        | ({readonly discriminator: 'IntegerLiteral'} & IntegerLiteral)
        | ({readonly discriminator: 'StringLiteral'} & StringLiteral)
        | {readonly discriminator: 'NixLiteral'};

    export type BooleanLiteral =
        | {readonly discriminator: 'BooleanLiteralFalse'}
        | {readonly discriminator: 'BooleanLiteralTrue'};

    export interface IntegerLiteral {
        readonly value: number;
    }

    export interface StringLiteral {
        readonly value: string;
    }

    export type AssignableExpression =
        | ({readonly discriminator: 'ThisExpression'} & ThisExpression)
        | ({readonly discriminator: 'VariableAccess'} & VariableAccess)
        | ({readonly discriminator: 'MemberAccess'} & MemberAccess);

    export interface ThisExpression {
        readonly type: string;
        readonly isExplicitly: boolean;
    }

    export interface VariableAccess {
        readonly variable: Variable;
    }

    export interface MemberAccess {
        readonly operand: Expression;
        readonly clazz: string;
        readonly member: Member;
    }

    export type InvocationExpression =
        | ({readonly discriminator: 'BuiltinMethodInvocation'} & BuiltinMethodInvocation)
        | ({readonly discriminator: 'MethodInvocation'} & MethodInvocation)
        | ({readonly discriminator: 'ObjectAllocation'} & ObjectAllocation);

    export interface BuiltinMethodInvocation {
        readonly method: BuiltinMethod;
        readonly arguments: ReadonlyArray<Expression>;
    }

    export interface MethodInvocation {
        readonly operand: Expression;
        readonly clazz: string;
        readonly methodSignature: MethodSignature;
        readonly arguments: ReadonlyArray<Expression>;
    }

    export interface ObjectAllocation {
        readonly type: string;
        readonly constructorSignature: ConstructorSignature;
        readonly arguments: ReadonlyArray<Expression>;
    }

    export type JasminInstruction =
        | DotInstruction
        | ({readonly discriminator: 'Label'} & Label)
        | {readonly discriminator: 'AconstNull'}
        | ({readonly discriminator: 'Aload'} & Aload)
        | {readonly discriminator: 'Aload0'}
        | ({readonly discriminator: 'Getstatic'} & Getstatic)
        | ({readonly discriminator: 'Goto'} & Goto)
        | ({readonly discriminator: 'Iload'} & Iload)
        | Ldc
        | ({readonly discriminator: 'New'} & New)
        | {readonly discriminator: 'Return'}
        | {readonly discriminator: 'Areturn'}
        | ({readonly discriminator: 'Astore'} & Astore)
        | {readonly discriminator: 'Astore0'}
        | {readonly discriminator: 'Dup'}
        | ({readonly discriminator: 'Getfield'} & Getfield)
        | ({readonly discriminator: 'Ifeq'} & Ifeq)
        | ({readonly discriminator: 'Ifne'} & Ifne)
        | {readonly discriminator: 'Ineg'}
        | {readonly discriminator: 'Ireturn'}
        | ({readonly discriminator: 'Istore'} & Istore)
        | {readonly discriminator: 'Pop'}
        | ({readonly discriminator: 'Putstatic'} & Putstatic)
        | {readonly discriminator: 'Iadd'}
        | {readonly discriminator: 'Iand'}
        | {readonly discriminator: 'Idiv'}
        | ({readonly discriminator: 'IfAcmpeq'} & IfAcmpeq)
        | ({readonly discriminator: 'IfAcmpne'} & IfAcmpne)
        | ({readonly discriminator: 'IfIcmpeq'} & IfIcmpeq)
        | ({readonly discriminator: 'IfIcmpne'} & IfIcmpne)
        | ({readonly discriminator: 'IfIcmpge'} & IfIcmpge)
        | ({readonly discriminator: 'IfIcmpgt'} & IfIcmpgt)
        | ({readonly discriminator: 'IfIcmple'} & IfIcmple)
        | ({readonly discriminator: 'IfIcmplt'} & IfIcmplt)
        | {readonly discriminator: 'Imul'}
        | {readonly discriminator: 'Ior'}
        | {readonly discriminator: 'Irem'}
        | {readonly discriminator: 'Ishl'}
        | {readonly discriminator: 'Ishr'}
        | {readonly discriminator: 'Isub'}
        | ({readonly discriminator: 'Putfield'} & Putfield)
        | ({readonly discriminator: 'Invokespecial'} & Invokespecial)
        | ({readonly discriminator: 'Invokestatic'} & Invokestatic)
        | ({readonly discriminator: 'Invokevirtual'} & Invokevirtual);

    export type DotInstruction =
        | ({readonly discriminator: 'DotSource'} & DotSource)
        | ({readonly discriminator: 'DotClass'} & DotClass)
        | ({readonly discriminator: 'DotSuper'} & DotSuper)
        | ({readonly discriminator: 'DotLimitLocals'} & DotLimitLocals)
        | ({readonly discriminator: 'DotLimitStack'} & DotLimitStack)
        | ({readonly discriminator: 'DotField'} & DotField)
        | ({readonly discriminator: 'DotMethod'} & DotMethod)
        | {readonly discriminator: 'DotEndMethod'};

    export interface DotSource {
        readonly sourceFileName: string;
    }

    export interface DotClass {
        readonly classId: string;
    }

    export interface DotSuper {
        readonly classId: string;
    }

    export interface DotLimitLocals {
        readonly value: number;
    }

    export interface DotLimitStack {
        readonly value: number;
    }

    export interface DotField {
        readonly member: Member;
        readonly isStatic: boolean;
    }

    export interface DotMethod {
        readonly accessModifier: AccessModifier;
        readonly isStatic: boolean;
        readonly signature: JasminSignature;
    }

    export interface Label {
        readonly name: string;
    }

    export interface Aload {
        readonly index: number;
        readonly id: string;
    }

    export interface Getstatic {
        readonly arguments: string;
    }

    export interface Goto {
        readonly label: Label;
    }

    export interface Iload {
        readonly index: number;
        readonly id: string;
    }

    export type Ldc =
        | ({readonly discriminator: 'LdcInt'} & LdcInt)
        | ({readonly discriminator: 'LdcString'} & LdcString);

    export interface LdcInt {
        readonly value: number;
    }

    export interface LdcString {
        readonly value: string;
    }

    export interface New {
        readonly arguments: string;
    }

    export interface Astore {
        readonly index: number;
        readonly id: string;
    }

    export interface Getfield {
        readonly classId: string;
        readonly id: string;
        readonly type: JasminType;
    }

    export interface Ifeq {
        readonly label: Label;
    }

    export interface Ifne {
        readonly label: Label;
    }

    export interface Istore {
        readonly index: number;
        readonly id: string;
    }

    export interface Putstatic {
        readonly arguments: string;
    }

    export interface IfAcmpeq {
        readonly label: Label;
    }

    export interface IfAcmpne {
        readonly label: Label;
    }

    export interface IfIcmpeq {
        readonly label: Label;
    }

    export interface IfIcmpne {
        readonly label: Label;
    }

    export interface IfIcmpge {
        readonly label: Label;
    }

    export interface IfIcmpgt {
        readonly label: Label;
    }

    export interface IfIcmple {
        readonly label: Label;
    }

    export interface IfIcmplt {
        readonly label: Label;
    }

    export interface Putfield {
        readonly classId: string;
        readonly id: string;
        readonly type: JasminType;
    }

    export interface Invokespecial {
        readonly classId: string;
        readonly argumentTypes: ReadonlyArray<JasminValueType>;
    }

    export interface Invokestatic {
        readonly classId: string;
        readonly signature: JasminSignature;
    }

    export interface Invokevirtual {
        readonly classId: string;
        readonly signature: JasminSignature;
    }

    export type JasminType =
        | JasminValueType
        | {readonly discriminator: 'JasminVoid'};

    export type JasminValueType =
        | {readonly discriminator: 'JasminBool'}
        | {readonly discriminator: 'JasminInt'}
        | ({readonly discriminator: 'JasminReference'} & JasminReference);

    export interface JasminReference {
        readonly id: string;
    }

    export type PhaseMessage =
        | ({readonly discriminator: 'LexerError'} & LexerError)
        | ({readonly discriminator: 'ParserError'} & ParserError)
        | TypeError
        | TypeWarning;

    export interface LexerError {
        readonly location: Location;
        readonly text: string;
    }

    export interface ParserError {
        readonly location: Location;
        readonly text: string;
    }

    export type TypeError =
        | ({readonly discriminator: 'BinaryTypeError'} & BinaryTypeError)
        | ({readonly discriminator: 'CannotInvokeError'} & CannotInvokeError)
        | ({readonly discriminator: 'ClassDoubleDefinitionTypeError'} & ClassDoubleDefinitionTypeError)
        | ({readonly discriminator: 'DoesNotHaveMemberError'} & DoesNotHaveMemberError)
        | ({readonly discriminator: 'IncompatibleConditionTypeError'} & IncompatibleConditionTypeError)
        | ({readonly discriminator: 'IncompatibleReturnTypeError'} & IncompatibleReturnTypeError)
        | ({readonly discriminator: 'MainInstantiationError'} & MainInstantiationError)
        | ({readonly discriminator: 'MainMemberError'} & MainMemberError)
        | ({readonly discriminator: 'MemberAccessError'} & MemberAccessError)
        | ({readonly discriminator: 'MemberDoubleDefinitionTypeError'} & MemberDoubleDefinitionTypeError)
        | ({readonly discriminator: 'MethodAccessError'} & MethodAccessError)
        | ({readonly discriminator: 'MethodDoubleDefinitionTypeError'} & MethodDoubleDefinitionTypeError)
        | ({readonly discriminator: 'UnaryTypeError'} & UnaryTypeError)
        | ({readonly discriminator: 'UndefinedIdError'} & UndefinedIdError)
        | ({readonly discriminator: 'UndefinedMethodError'} & UndefinedMethodError)
        | ({readonly discriminator: 'UnknownTypeError'} & UnknownTypeError)
        | ({readonly discriminator: 'VariableDoubleDefinitionTypeError'} & VariableDoubleDefinitionTypeError);

    export interface BinaryTypeError {
        readonly location: Location;
        readonly lhsType: string;
        readonly rhsType: string;
        readonly operator: string;
        readonly text: string;
    }

    export interface CannotInvokeError {
        readonly location: Location;
        readonly classId: string;
        readonly signatureNullable: SignatureNullable;
        readonly text: string;
    }

    export interface ClassDoubleDefinitionTypeError {
        readonly location: Location;
        readonly classId: string;
        readonly text: string;
    }

    export interface DoesNotHaveMemberError {
        readonly location: Location;
        readonly classId: string;
        readonly memberId: string;
        readonly text: string;
    }

    export interface IncompatibleConditionTypeError {
        readonly location: Location;
        readonly type: string;
        readonly text: string;
    }

    export interface IncompatibleReturnTypeError {
        readonly location: Location;
        readonly type: string;
        readonly text: string;
    }

    export interface MainInstantiationError {
        readonly location: Location;
        readonly text: string;
    }

    export interface MainMemberError {
        readonly location: Location;
        readonly text: string;
    }

    export interface MemberAccessError {
        readonly location: Location;
        readonly memberId: string;
        readonly classId: string;
        readonly text: string;
    }

    export interface MemberDoubleDefinitionTypeError {
        readonly location: Location;
        readonly memberId: string;
        readonly memberType: string;
        readonly classId: string;
        readonly text: string;
    }

    export interface MethodAccessError {
        readonly location: Location;
        readonly signature: Signature;
        readonly classId: string;
        readonly text: string;
    }

    export interface MethodDoubleDefinitionTypeError {
        readonly location: Location;
        readonly signature: Signature;
        readonly classId: string;
        readonly text: string;
    }

    export interface UnaryTypeError {
        readonly location: Location;
        readonly type: string;
        readonly operator: string;
        readonly text: string;
    }

    export interface UndefinedIdError {
        readonly location: Location;
        readonly id: string;
        readonly text: string;
    }

    export interface UndefinedMethodError {
        readonly location: Location;
        readonly signatureNullable: SignatureNullable;
        readonly text: string;
    }

    export interface UnknownTypeError {
        readonly location: Location;
        readonly classId: string;
        readonly text: string;
    }

    export interface VariableDoubleDefinitionTypeError {
        readonly location: Location;
        readonly variableId: string;
        readonly variableType: string;
        readonly signature: Signature;
        readonly text: string;
    }

    export type TypeWarning =
        | ({readonly discriminator: 'BinaryCoercionWarning'} & BinaryCoercionWarning)
        | ({readonly discriminator: 'ConditionCoercionWarning'} & ConditionCoercionWarning)
        | ({readonly discriminator: 'ReturnCoercionWarning'} & ReturnCoercionWarning)
        | ({readonly discriminator: 'UnaryCoercionWarning'} & UnaryCoercionWarning);

    export interface BinaryCoercionWarning {
        readonly location: Location;
        readonly operator: string;
        readonly foundLhs: string;
        readonly foundRhs: string;
        readonly resultLhs: string;
        readonly resultRhs: string;
        readonly text: string;
    }

    export interface ConditionCoercionWarning {
        readonly location: Location;
        readonly foundType: string;
        readonly resultType: string;
        readonly text: string;
    }

    export interface ReturnCoercionWarning {
        readonly location: Location;
        readonly foundType: string;
        readonly resultType: string;
        readonly text: string;
    }

    export interface UnaryCoercionWarning {
        readonly location: Location;
        readonly operator: string;
        readonly foundType: string;
        readonly resultType: string;
        readonly text: string;
    }

    export type ReplaceExpressionReason =
        | ({readonly discriminator: 'CoercionReplaceExpressionReason'} & CoercionReplaceExpressionReason)
        | ({readonly discriminator: 'VariableReplaceExpressionReason'} & VariableReplaceExpressionReason)
        | ({readonly discriminator: 'PropagationReplaceExpressionReason'} & PropagationReplaceExpressionReason)
        | ({readonly discriminator: 'RuleReplaceExpressionReason'} & RuleReplaceExpressionReason);

    export interface CoercionReplaceExpressionReason {
        readonly old: string;
        readonly replacement: string;
    }

    export interface VariableReplaceExpressionReason {
        readonly variable: Variable;
    }

    export interface PropagationReplaceExpressionReason {
        readonly mapping: ReadonlyArray<MappingEntry>;
        readonly variable: Variable;
    }

    export interface RuleReplaceExpressionReason {
        readonly old: string;
        readonly replacement: string;
    }

    export interface BinaryOperands {
        readonly lhs: Expression;
        readonly rhs: Expression;
    }
}
