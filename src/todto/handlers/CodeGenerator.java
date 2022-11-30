package todto.handlers;

import static java.lang.String.format;

import java.util.List;

public class CodeGenerator {

    
    public static String generateFromLower(TypeProxy copyUtil, 
                                        TypeProxy upper, 
                                        TypeProxy lower) {
        addImport(copyUtil, upper);
        addImport(copyUtil, lower);

        String upperTypeName = upper.getName();
        String upperVariableName = upper.getVariableName();
        String methodName = createFromLowerMethodName(lower);
        String lowerTypeName = lower.getName();
        String lowerVariableName = lower.getVariableName();
        
        
        List<FieldProxy> upperFields = upper.getFields();
        String methodContent = "";
        String upperOnlyFields = "";
        String deepCopyFields = "";
        
        methodContent += format("public static %s %s(%s %s) {\n", upperTypeName, methodName, lowerTypeName, lowerVariableName);
        methodContent += format("\t%s %s = new %s();\n", upperTypeName, upperVariableName, upperTypeName);
        for (FieldProxy field: upperFields) {
            String fieldName = field.getName();
            debug("%s,%s,%s,m=%s", fieldName, field.getSetterMethodName(), field.getGetterMethodName(), field.getMaskType());
            
            FieldProxy lowerField = lower.getField(fieldName);
            if (lowerField == null) {
                upperOnlyFields += format("\t//%s.%s();\n", upperVariableName, field.getSetterMethodName());
                continue;
            }
            if (lowerField.needDeepCopy()) {
                deepCopyFields += format("\t//%s.%s();\n", upperVariableName, field.getSetterMethodName());
                continue;
            } 
            if (field.hasMaskAnnotation()) {
                methodContent += format("\t%s.%s(EncryptionUtil.encrypt(%s.%s()));\n", upperVariableName, field.getSetterMethodName(), lowerVariableName, field.getGetterMethodName());
            } else {
                methodContent += format("\t%s.%s(%s.%s());\n", upperVariableName, field.getSetterMethodName(), lowerVariableName, field.getGetterMethodName());
            }

        }
        
        methodContent += format("\n\t//SKIP: %s에만 있는 필드\n", upperTypeName);
        methodContent += upperOnlyFields;
        methodContent += "\t//SKIP: Deep Copy는 제외\n";
        methodContent += deepCopyFields;
        methodContent += format("\treturn %s;\n", upperVariableName);
        methodContent += "}\n";
        debug("\n\n%s\n\n", methodContent);
        
        copyUtil.createMethod(methodContent);
        return format("Created %s()", methodName);

    }

    private static void addImport(TypeProxy copyUtil, TypeProxy vo) {
        copyUtil.addImport(vo.getFullName());
    }

    public static String generateToLower(TypeProxy copyUtil, 
                                        TypeProxy upper, 
                                        TypeProxy lower) {
        addImport(copyUtil, upper);
        addImport(copyUtil, lower);
        
        List<FieldProxy> lowerFields = upper.getFields();
        
        String methodContent = "";
        String lowerOnlyFields = "";
        String deepCopyFields = "";

        String upperTypeName = upper.getName();
        String upperVariableName = upper.getVariableName();
        String lowerTypeName = lower.getName();
        String methodName = createToLowerMethodName(lower);
        String lowerVariableName = lower.getVariableName();
        
        methodContent += format("public %s %s(%s %s) {\n", lowerTypeName, methodName, upperTypeName, upperVariableName);
        methodContent += format("\t%s %s = new %s();\n", lowerTypeName, lowerVariableName, lowerTypeName);
        for (FieldProxy field: lowerFields) {
            String fieldName = field.getName();
            FieldProxy upperField = upper.getField(fieldName);

            if (upperField == null) {
                lowerOnlyFields += format("\t//%s.%s();\n", lowerVariableName, field.getSetterMethodName());
                continue;
            }
            if (upperField.needDeepCopy()) {
                deepCopyFields += format("\t//%s.%s();\n", lowerVariableName, field.getSetterMethodName());
                continue;
            } 
            methodContent += format("\t%s.%s(%s.%s());\n", lowerVariableName, field.getSetterMethodName(), upperVariableName, field.getGetterMethodName());
        }     

        methodContent += format("\n\t//SKIP: %s에만 있는 필드\n", lowerTypeName);
        methodContent += lowerOnlyFields;
        methodContent += "\t//SKIP: Deep Copy는 제외\n";
        methodContent += deepCopyFields;
        methodContent += format("\treturn %s;\n", lowerVariableName);
        methodContent += "}\n";
        debug("\n\n%s\n\n", methodContent);
        copyUtil.createMethod(methodContent);   
        
        return format("Created %s()", methodName);
    }

    public static String createToLowerMethodName(TypeProxy lower) {
        return format("to%s", lower.getTypeString());
    }

    public static String createFromLowerMethodName(TypeProxy lower) {
        return format("from%s", lower.getTypeString());
    }

    private static void debug(String format, Object...vars) {
        System.out.println(String.format(format, vars));
    }
}
