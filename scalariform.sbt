import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := FormattingPreferences()
.setPreference(RewriteArrowSymbols, true)
.setPreference(AlignParameters, true)
.setPreference(AlignSingleLineCaseStatements, true)
.setPreference(DoubleIndentClassDeclaration, true)