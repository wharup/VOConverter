package todto.handlers;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;

public class FieldProxy {
    private String type;
    private String name;
    
    public FieldProxy(IField field) {
        try {
            this.type = field.getTypeSignature();
        } catch (JavaModelException e) {
            e.printStackTrace();
            throw new ConvertException("알수 없는 에러가 발생했습니다.");
        }
        this.name = field.getElementName();;
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

    public String toString() {
        return "FieldProxy [type=" + type + ", name=" + name + "]";
    }
    
}