package todto.handlers;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;

public class FieldProxy {
    private String type;
    private String name;
    private String maskType;
    
    public String getMaskType() {
        return maskType;
    }

    public FieldProxy(IField field) {
        try {
            this.type = field.getTypeSignature();
        } catch (JavaModelException e) {
            e.printStackTrace();
            throw new ConvertException("알수 없는 에러가 발생했습니다.");
        }
        this.name = field.getElementName();;
        getMaskedType(field);
    }

    private static void debug(String format, Object...vars) {
        System.out.println(String.format(format, vars));
    }

    private void getMaskedType(IField field) {
        try {
            IAnnotation[] annotations = field.getAnnotations();
            for (IAnnotation a : annotations) {
                debug("> %s", a.getElementName());
            }
        } catch (JavaModelException e1) {
            e1.printStackTrace();
        }
        this.maskType = findAnnotation(field, "Masked");
    }

    //field.getAnnotation("Masked")의 버그(?)로 별도 메소드 만듦
    //Annotation이 있는 상태로 코드 생성하고, Annotation을 지운 후에 코드 생성하면
    //없어진 Annotation이 조회됨  
    private String findAnnotation(IField field, String name) {
        try {
            for (IAnnotation a : field.getAnnotations()) {
                if (name.equals(a.getElementName())) {
                    IMemberValuePair[] memberValuePairs = a.getMemberValuePairs();
                    for ( int i = 0; i < memberValuePairs.length; i++) {
                        if ("value".equals(memberValuePairs[i].getMemberName())) {
                            return (String)memberValuePairs[0].getValue();
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getName() {
        return name; 
    }
    
    public String getGetterMethodName() {
        return methodName("get");
    }

    public String getSetterMethodName() {
        return methodName("set");
    }
    
    private String methodName(String string) {
        String result = string + name.substring(0, 1).toUpperCase();
        if (name.length() > 1) {
            result += name.substring(1);
        }
        return result;
    }

    public boolean needDeepCopy() {
        String[] words = type.split("<");
        switch (words[0]) {
        case "QList":
        case "QMap":
        case "QSet":
        case "QArrayList":
        case "QHashMap":
        case "QHashSet":
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FieldProxy [type=" + type + ", name=" + name + ", maskType=" + maskType + "]";
    }

    public boolean hasMaskAnnotation() {
        return maskType != null && !maskType.isEmpty();
    }
    
}