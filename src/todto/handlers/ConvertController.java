package todto.handlers;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConvertController {

    TypeProxy svo = null;
    TypeProxy dvo = null;
    
    public String generateConvertMethods(ExecutionEvent event) {
        List<ICompilationUnit> units = getSelectedSVOAndDVO(event);
        
        makeSureSVOAndDVOAreFound(units);

        makeSureSVOMethodsAreNotExist(units);

        List<String> operations = new ArrayList<>();
        operations.add(CodeGenerator.generateToDVO(dvo, svo));
        operations.add(CodeGenerator.generateFromDVO(dvo, svo));

        return msgSuccessGenerateMethods(operations);
    }

    private void makeSureSVOMethodsAreNotExist(List<ICompilationUnit> units) {
        if (svo.hasToDVO(dvo) && svo.hasFromDVO(dvo)) {
            throw new ConvertException(msgHasAllMethodsAlready());
        }
    }

    private void makeSureSVOAndDVOAreFound(List<ICompilationUnit> units) {
        if (svo == null || dvo == null) {
            throw new ConvertException(msgCannotFindSVODVO(units));
        }
    }

    private List<ICompilationUnit> getSelectedSVOAndDVO(ExecutionEvent event) {
        List<ICompilationUnit> types = getSelectedClasses(event);
        int index = 0;
        debug("----- ----- ----- ----- ----- -----");
        for (ICompilationUnit type : types) {
            String name = type.getElementName().replace(".java", "");
            if(name.endsWith("SVO")) {
                svo = new TypeProxy(this, type);
            } else if (name.endsWith("DVO")) {
                dvo = new TypeProxy(this, type);
            } else {
                debug("UNIDENTIFIED TYPE FOUND! : %s", type.getElementName());
            }
        }
        debug("----- ----- ----- ----- ----- -----");
        debug(" SVO: %s\n", svo.toString());
        debug(" DVO: %s\n", dvo.toString());
        debug("----- ----- ----- ----- ----- -----");
        return types;
    }
    
    private List<ICompilationUnit> getSelectedClasses(ExecutionEvent event) {
        IWorkbenchWindow window;
        try {
            window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new ConvertException("알수 없는 에러가 발생했습니다.");
        }
        ISelectionService service = window.getSelectionService();
        ISelection selection = service.getSelection();
        if (selection instanceof IStructuredSelection) {
        } else {
            throw new ConvertException("Project Explorer에서 클래스를 선택해주세요.");
        }
        IStructuredSelection structured = (IStructuredSelection)selection;

        List<ICompilationUnit> result = new ArrayList<>();
        Iterator itr = structured.iterator();
        while (itr.hasNext()) {
            Object selected = itr.next();
            if (selected instanceof ICompilationUnit) {
                ICompilationUnit icu = (ICompilationUnit) selected;
                result.add(icu);
            }
        }
        return result;
    }
    

    private String msgSuccessGenerateMethods(List<String> operations) {
        String result = "";
           result += format("메소드 생성에 성공하였습니다.\n");
           result += format("SVO = %s\n", svo.getName());
           result += format("DVO = %s\n ", dvo.getName());
           result += format("\n생성 결과:\n");
           for (String operation : operations) {
               result += format("\t%s\n", operation);
           }
        return result;
    }

    private String msgCannotFindSVODVO(List<ICompilationUnit> units) {
        String msg = "";
        msg += format("SVO, DVO를 찾지 못했습니다!\n");
        msg += format("SVO = %s\n", svo.getFullName());
        msg += format("DVO = %s\n ", dvo.getFullName());
        msg += "\n";
        msg += format("> found types (%d)\n", units.size());
        int i = 0;
        for (ICompilationUnit unit : units) {
            msg += format("  %d:%s\n", i++, unit.getElementName());
        }
        return msg;
    }

    private String msgHasAllMethodsAlready() {
        String msg = "";
        msg += format("SVO에 생성할 메소드가 모두 존재합니다! (toDVO, fromDVO)\n");
        msg += format("\n");
        msg += format("SVO = %s\n", svo.getFullName());
        msg += format("DVO = %s\n", dvo.getFullName());
        msg += "\n";
        return msg;
    }
    
    void debug(String format, Object...vars) {
        System.out.println(String.format(format, vars));
    }
}
