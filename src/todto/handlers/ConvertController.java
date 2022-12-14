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
                svo = new TypeProxy(type);
            } else if (name.endsWith("DVO")) {
                dvo = new TypeProxy(type);
            } else {
                debug("UNIDENTIFIED TYPE FOUND! : %s", type.getElementName());
            }
        }
        debug("----- ----- ----- ----- ----- -----");
        debug(" SVO: %s\n", getFullName(svo));
        debug(" DVO: %s\n", getFullName(dvo));
        debug("----- ----- ----- ----- ----- -----");
        return types;
    }

    private String getFullName(TypeProxy type) {
        return type == null ? "NOT_FOUND" : type.getFullName();
    }
    
    private String getName(TypeProxy type) {
        return type == null ? "NOT_FOUND" : type.getName();
    }
    
    private List<ICompilationUnit> getSelectedClasses(ExecutionEvent event) {
        IWorkbenchWindow window;
        try {
            window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new ConvertException("?????? ?????? ????????? ??????????????????.");
        }
        ISelectionService service = window.getSelectionService();
        ISelection selection = service.getSelection();
        if (selection instanceof IStructuredSelection) {
        } else {
            throw new ConvertException("Project Explorer?????? ???????????? ??????????????????.");
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
           result += format("????????? ????????? ?????????????????????.\n");
           result += format("SVO = %s\n", getName(svo));
           result += format("DVO = %s\n ", getName(dvo));
           result += format("\n?????? ??????:\n");
           for (String operation : operations) {
               result += format("\t%s\n", operation);
           }
        return result;
    }

    private String msgCannotFindSVODVO(List<ICompilationUnit> units) {
        String msg = "";
        msg += format("SVO, DVO??? ?????? ???????????????!\n");
        msg += format("SVO = %s\n", getFullName(svo));
        msg += format("DVO = %s\n ", getFullName(dvo));
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
        msg += format("SVO??? ????????? ???????????? ?????? ???????????????! (toDVO, fromDVO)\n");
        msg += format("\n");
        msg += format("SVO = %s\n", getFullName(svo));
        msg += format("DVO = %s\n", getFullName(dvo));
        msg += "\n";
        return msg;
    }
    
    void debug(String format, Object...vars) {
        System.out.println(String.format(format, vars));
    }
}
