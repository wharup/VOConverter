package todto.handlers;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class TypeProxy {
    IType type = null;
    private ICompilationUnit icu;
    
    public TypeProxy(ICompilationUnit icu) {
        this.icu = icu;
        this.type = getPrimaryType(icu);
    }
    
    protected IType getPrimaryType(ICompilationUnit icu) {
        try {
            IType[] types = icu.getTypes();
            return types[0];
        } catch (JavaModelException e) {
            throw new ConvertException("알수 없는 에러가 발생했습니다.");
        }
    }

    private List<String> getMethods() {
        List<String> result = new ArrayList<>();
        
        try {
            IMethod[] methods = type.getMethods();
            for (IMethod m : methods) {
                debug("%s", m.getKey());
                result.add(m.getKey());
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
            throw new ConvertException("알수 없는 에러가 발생했습니다.");
        }
        return result;
    }

    public boolean hasFromDVO(TypeProxy dvo) {
        //LCustSVO;.fromDVO(QCustDVO;)V
        String svoName = getFullName().replace(".", "/");
        String dvoName = dvo.getName().replace(".", "/");
        String toDVO = format("L%s;.fromDVO(Q%s;)V", svoName, dvoName);
        List<String> methods = getMethods();
        for (String method : methods) {
            debug("%s, %s", method, toDVO);
            if (method.equals(toDVO)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasToDVO(TypeProxy dvo) {
        //LCustSVO;.fromDVO(QCustDVO;)V
        String fromDVO = format("L%s;.toDVO()V", getFullName().replace(".", "/"));
        List<String> methods = getMethods();
        for (String method : methods) {
            debug("%s, %s", method, fromDVO);
            if (method.equals(fromDVO)) {
                return true;
            }
        }
        return false;
    }
    
    public String getFullName() {
        return type != null ? type.getFullyQualifiedName() : "NOT FOUND";
    }
    
    public String getName() {
        return type != null ? type.getElementName() : "NOT FOUND";
    }

    public FieldProxy getField(String name) {
        List<FieldProxy> fields = getFields();
        for (FieldProxy f : fields) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
    
    public List<FieldProxy> getFields() {
        List<FieldProxy> fields = new ArrayList<>(); 
        try {
            IField[] svoFields = type.getFields();
            if (svoFields.length == 0) {
                throw new ConvertException("클래스에 필드 변수가 없습니다. " + type.getElementName());
            }
            for (IField f : svoFields) {
                fields.add(new FieldProxy(f));
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
            throw new ConvertException("알수 없는 에러가 발생했습니다.");
        }
        return fields;
    }

    @Override
    public String toString() {
        String result = "";
        int index = 0;
        result += format("TypeProxy [type=%s]\n", getFullName());
        for(FieldProxy f : getFields()) {
            result += format("\t%d:%s\n", index++, f);
        }
        return result;
    }

    public void createMethod(String method) {
        try {
            type.createMethod(method, null, false, null);
        } catch (JavaModelException e) {
            e.printStackTrace();
            throw new ConvertException("메소드 생성 중에 알수 없는 에러가 발생했습니다.");
        }
    }
    
    public void addImport(String method) {
        try {
            icu.createImport(method, null, null);
        } catch (JavaModelException e) {
            e.printStackTrace();
            throw new ConvertException("Import 구문 생성 중에 알수 없는 에러가 발생했습니다.");
        }
    }
    
    private static void debug(String format, Object...vars) {
        System.out.println(String.format(format, vars));
    }
}