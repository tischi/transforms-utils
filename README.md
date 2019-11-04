# transforms-utils

## LSLF Registration CLI

Example call:

```
java -jar lslfRegistration.jar "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LF_stack.tif" "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LS_stack.tif" "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/XML_fromMultiviewRegistrationPlugin/dataset.xml" "0,0,0" "500,1000,300" "1,1,20" "Linear"
```

Build the jar:

```
mvn clean package
```

