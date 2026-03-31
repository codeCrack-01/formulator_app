package dimensions

sealed class DimExpr

data class DimConst(val dim: DimensionVector) : DimExpr()
data class DimUnknown(val v: DimVar) : DimExpr()

data class DimAdd(val a: DimExpr, val b: DimExpr) : DimExpr()
data class DimSub(val a: DimExpr, val b: DimExpr) : DimExpr()