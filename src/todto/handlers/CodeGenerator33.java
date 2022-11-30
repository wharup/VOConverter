package todto.handlers;

import static java.lang.String.format;

import java.util.List;

public class CodeGenerator33 {

    
    public static String generateFromDVO(TypeProxy dvo, TypeProxy svo) {
        if (svo.hasFromDVO(dvo)) {
            return "NOT CREATED : fromDVO() exists!";
        }
        addImport(svo, dvo);

        String svoName = svo.getName();
        String dvoName = dvo.getName();

        List<FieldProxy> svoFields = svo.getFields();
        String method = "";
        String svoOnlyFields = "";
        String deepCopyFields = "";
        
        method += format("public static %s fromDVO(%s dvo) {\n", svoName, dvoName);
        method += format("\t%s svo = new %s();\n", svoName, svoName);
        for (FieldProxy field: svoFields) {
            String fieldName = field.getName();
            debug("%s,%s,%s,m=%s", fieldName, field.getSetterMethodName(), field.getGetterMethodName(), field.getMaskType());
            
            FieldProxy dvoField = dvo.getField(fieldName);
            if (dvoField == null) {
                svoOnlyFields += format("\t//svo.%s();\n", field.getSetterMethodName());
                continue;
            }
            if (dvoField.needDeepCopy()) {
                deepCopyFields += format("\t//svo.%s();\n", field.getSetterMethodName());
                continue;
            } 
            if (field.hasMaskAnnotation()) {
                method += format("\tsvo.%s(EncryptionUtil.encrypt(dvo.%s()));\n", field.getSetterMethodName(), field.getGetterMethodName());
            } else {
                method += format("\tsvo.%s(dvo.%s());\n", field.getSetterMethodName(), field.getGetterMethodName());
            }

        }
        
        method += "\n\t//SKIP: SVO에만 있는 필드\n";
        method += svoOnlyFields;
        method += "\t//SKIP: Deep Copy는 제외\n";
        method += deepCopyFields;
        method += "\treturn svo;\n";
        method += "}\n";
        debug("\n\n%s\n\n", method);
        
//        svo.createMethod(method);
        return "Created fromDVO()";

    }

    private static void addImport(TypeProxy svo, TypeProxy dvo) {
        svo.addImport(dvo.getFullName());
    }

    public static String generateToDVO(TypeProxy dvo, TypeProxy svo) {
        if (svo.hasToDVO(dvo)) {
            return "NOT CREATED : toDVO() exists!";
        }
        addImport(svo, dvo);
        
        List<FieldProxy> dvoFields = dvo.getFields();
        
        String method = "";
        String dvoOnlyFields = "";
        String deepCopyFields = "";

        String dvoName = dvo.getName();
        method += format("public %s toDVO() {\n", dvoName);
        method += format("\t%s dvo = new %s();\n", dvoName, dvoName);
        for (FieldProxy field: dvoFields) {
            String fieldName = field.getName();
            FieldProxy svoField = svo.getField(fieldName);

            if (svoField == null) {
                dvoOnlyFields += format("\t//dvo.%s();\n", field.getSetterMethodName());
                continue;
            }
            if (svoField.needDeepCopy()) {
                deepCopyFields += format("\t//dvo.%s();\n", field.getSetterMethodName());
                continue;
            } 
            method += format("\tdvo.%s(%s());\n", field.getSetterMethodName(), field.getGetterMethodName());
        }     

        method += "\n\t//SKIP: DVO에만 있는 필드\n";
        method += dvoOnlyFields;
        method += "\t//SKIP: Deep Copy는 제외\n";
        method += deepCopyFields;
        method += "\treturn dvo;\n";
        method += "}\n";
        debug("\n\n%s\n\n", method);
//        svo.createMethod(method);   
        
        return "Created toDVO()";
    }

    private static void debug(String format, Object...vars) {
        System.out.println(String.format(format, vars));
    }
}
