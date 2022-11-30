package todto.handlers;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ConvertController {

    TypeProxy svo = null;
    TypeProxy dvo = null;
    
    public String generateConvertMethods(ExecutionEvent event) {
        debug("haha : %s", svo == null ? "null" : svo.toString());
        debug("haha : %s", dvo == null ? "null" : dvo.toString());
        svo = null;
        dvo = null;
                   
        List<ICompilationUnit> units = getSelectedSVOAndDVOFromProjectExplorer(event);
        
        makeSureSVOAndDVOAreFound(units);

        makeSureSVOMethodsAreNotExist(units);

        List<String> operations = new ArrayList<>();
//        operations.add(CodeGenerator.generateToDVO(dvo, svo));
        operations.add(CodeGenerator.generateFromDVO(dvo, svo));

        return msgSuccessGenerateMethods(operations);
    }

    public String generateAndConvertToCopyUtil(ExecutionEvent event) {
        debug("haha : %s", svo == null ? "null" : svo.toString());
        debug("haha : %s", dvo == null ? "null" : dvo.toString());
        svo = null;
        dvo = null;
                   
        List<ICompilationUnit> units = getSelectedSVOAndDVOFromActiveTextEditor(event);
        
        makeSureSVOAndDVOAreFound(units);

        makeSureSVOMethodsAreNotExist(units);

        List<String> operations = new ArrayList<>();
//        operations.add(CodeGenerator.generateToDVO(dvo, svo));
        operations.add(CodeGenerator.generateFromDVO(dvo, svo));

        return msgSuccessGenerateMethods(operations);
    }

    
    private List<ICompilationUnit> getSelectedSVOAndDVOFromActiveTextEditor(ExecutionEvent event) {
        List<ICompilationUnit> result = new ArrayList<>();
        
        try {               
            IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if ( part instanceof ITextEditor ) {
                final ITextEditor editor = (ITextEditor)part;
                IDocumentProvider prov = editor.getDocumentProvider();
                IDocument doc = prov.getDocument( editor.getEditorInput() );
                ISelection sel = editor.getSelectionProvider().getSelection();
                
                if ( sel instanceof TextSelection ) {
                    final TextSelection textSel = (TextSelection)sel;
//                    String newText = "/*" + textSel.getText() + "*/";
//                    doc.replace( textSel.getOffset(), textSel.getLength(), newText );
                    
                    
                    ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
                    ICompilationUnit icu = (ICompilationUnit) typeRoot.getAdapter(ICompilationUnit.class);
                    CompilationUnit cu = parse(icu);
                    NodeFinder finder = new NodeFinder(cu, textSel.getOffset(), textSel.getLength());
                    
                    ASTNode node = finder.getCoveringNode();
                    
                    ASTNode statement = node.getParent().getParent().getParent();
                    Block block = (Block)statement;
                    ASTNode method = node.getParent().getParent().getParent().getParent(); 
                    MethodDeclaration methodDeclaration = (MethodDeclaration)method;
                    
                    
                    Map<String, ITypeBinding> memberVariables = new HashMap<>();
                    
                    ASTVisitor visitor = new ASTVisitor() {
                        
                        public boolean visit(CompilationUnit node) {
                            debug("CompilationUnit %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(FieldAccess node) {
                            debug("FieldAccess %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(FieldDeclaration node) {
                            debug("FieldDeclaration %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(MemberRef node) {
                            debug("MemberRef %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(MemberValuePair node) {
                            debug("MemberValuePair %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(SimpleType node) {
                            debug("SimpleType %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(VariableDeclarationExpression node) {
                            debug("VariableDeclarationExpression %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(VariableDeclarationStatement node) {
                            debug("VariableDeclarationStatement %s", node.toString());
                            Type type = node.getType();
                            List fragments = node.fragments();
                            for (int i = 0; i < fragments.size(); i++) {
                                Object object = fragments.get(i);
                                if (object instanceof VariableDeclarationFragment) {
                                    VariableDeclarationFragment fragment = (VariableDeclarationFragment)object;
                                    SimpleName name = fragment.getName();
                                    ITypeBinding resolveBinding = type.resolveBinding();
                                    debug("found a type %s, %s, %s, %s", name.toString(), name.getFullyQualifiedName(), type.toString(), resolveBinding.getQualifiedName());
                                    memberVariables.put(name.toString(), resolveBinding);
                                    debug("\t%s = %s", name.toString(), type.toString());
                                }
                            }
                            return super.visit(node);
                        }
                        public boolean visit(VariableDeclarationFragment node) {
                            debug("VariableDeclarationFragment %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(Assignment node) {
                            debug("Assignment %s", node.toString());
                            return super.visit(node);
                        }
                        public boolean visit(MethodDeclaration node) {
                            List parameters = node.parameters();
                            for (int i = 0; i < parameters.size(); i++) {
                                Object object = parameters.get(i);
                                if (object instanceof SingleVariableDeclaration) {
                                    SingleVariableDeclaration variable = (SingleVariableDeclaration)object;
                                    SimpleName name = variable.getName();
                                    Type type = variable.getType();
                                    ITypeBinding resolveBinding = type.resolveBinding();
                                    debug("found a type %s, %s, %s, %s", name.toString(), name.getFullyQualifiedName(), type.toString(), resolveBinding.getQualifiedName());
                                    memberVariables.put(name.toString(), resolveBinding);
                                }
                            }
                            SimpleName name = node.getName();
                            debug("MethodDeclaration %s", node.toString());
                            return super.visit(node);
                        }
                        
                    };
                    methodDeclaration.accept(visitor);
                    for(String name : memberVariables.keySet()) {
                        ITypeBinding type = memberVariables.get(name);
                        debug("%s, %s", name.toString(), type.toString());
                    }
                    
                    IJavaProject javaProject = icu.getJavaProject(); 
                    if ( node.getParent() instanceof MethodInvocation) {
                        MethodInvocation methodInvocation = (MethodInvocation) node.getParent();
                        List arguments = methodInvocation.arguments();
                        Object object1 = arguments.get(0);
                        Object object2 = arguments.get(1);
                        ITypeBinding result1 = memberVariables.get(object1.toString());
                        ITypeBinding result2 = memberVariables.get(object2.toString());
                        debug("%s, %s", object1.toString(), result1.getQualifiedName());
                        debug("%s, %s", object2.toString(), result2.getQualifiedName());
                        
                        IType findType1 = javaProject.findType(result1.getQualifiedName());
                        IType findType2 = javaProject.findType(result2.getQualifiedName());
                        
                        ICompilationUnit compilationUnit1 = findType1.getCompilationUnit();
                        ICompilationUnit compilationUnit2 = findType2.getCompilationUnit();
                        result.add(compilationUnit1);
                        result.add(compilationUnit2);
                    }
                    String copyUtilName = "";
                    debug("----- ----- ----- ----- ----- -----");
                    for (ICompilationUnit type : result) {
                        String name = type.getElementName().replace(".java", "");
                        if(name.endsWith("SVO")) {
                            print(type);
                            svo = new TypeProxy(type);
                            copyUtilName = name.substring(0,  name.length() -3) + "CopyUtil";
                        } else if (name.endsWith("DVO")) {
                            dvo = new TypeProxy(type);
                        } else {
                            debug("UNIDENTIFIED TYPE FOUND! : %s", type.getElementName());
                        }
                    }
                    
                    IPath path = icu.getPath();
                    debug("%s", path.toString());
                    File file = path.toFile();
                    debug("%s", file.toString());
                    debug("%s", icu.getElementName());
                    String getCurrentWorkingDirectory = path.toString().replace(icu.getElementName(), "");
                    debug("%s", getCurrentWorkingDirectory);
                    String copyUtilFullPathname = getCurrentWorkingDirectory + copyUtilName +".java";
                    debug("%s", copyUtilFullPathname);
                    
                    createCopyUtil(copyUtilName, copyUtilFullPathname);
                    
                    debug("----- ----- ----- ----- ----- -----");
                    debug(" SVO: %s\n", getFullName(svo));
                    debug(" DVO: %s\n", getFullName(dvo));
                    debug(" UTIL: %sl\n", copyUtilName);
                    debug("----- ----- ----- ----- ----- -----");

                    
                    debug("%s", node.toString());
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }    
        
        return result;
    }

    private void createCopyUtil(String copyUtilName, String copyUtilFullPathname) {
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFile file = wsRoot.getFile(new Path(copyUtilFullPathname));
        if(file.exists()) {
        } else {
            debug("%s, %s", file.toString(), file.exists() ? "y" : "n");
        }

        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setSource(unit);
        CompilationUnit root = (CompilationUnit) parser.createAST(null);
        PackageDeclaration package1 = root.getPackage();
        ITypeRoot typeRoot = root.getTypeRoot();
        AST ast = root.getAST();
        
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newSimpleName("svo"));
        root.setPackage(packageDeclaration);
        ImportDeclaration importDeclaration = ast.newImportDeclaration();
        QualifiedName name = 
         ast.newQualifiedName(
          ast.newSimpleName("java"),
          ast.newSimpleName("util"));
        importDeclaration.setName(name);
        importDeclaration.setOnDemand(true);
        root.imports().add(importDeclaration);
        TypeDeclaration type = ast.newTypeDeclaration();
        type.setInterface(false);
        type.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        type.setName(ast.newSimpleName(copyUtilName));
        root.types().add(type);
        String fileContent = root.toString();
        debug("%s", fileContent);
        try {
            InputStream contents=new ByteArrayInputStream(fileContent.getBytes());
            file.create(contents, false, null);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private CompilationUnit parse(ICompilationUnit lwUnit) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(lwUnit); // set source
        parser.setResolveBindings(true); // we need bindings later on
        return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
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

    private List<ICompilationUnit> getSelectedSVOAndDVOFromProjectExplorer(ExecutionEvent event) {
        List<ICompilationUnit> types = getSelectedClasses(event);
        int index = 0;
        debug("----- ----- ----- ----- ----- -----");
        for (ICompilationUnit type : types) {
            String name = type.getElementName().replace(".java", "");
            if(name.endsWith("SVO")) {
                print(type);
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

    private void print(ICompilationUnit type) {
//        type.getTypes()[0].getFields()[2].getAnnotations()
        
        IField[] fields = null;
        try {
            fields = type.getTypes()[0].getFields();
            for (IField f : fields) {
//                f.getAnnotations().length[0].getElementName();
                IAnnotation[] annotations = f.getAnnotations();
                int length = annotations.length;
                debug("^^ %s, %s", f, length);
                for (int i = 0; i < length; i++) {
                    debug("DBG:  ^^ %s, %s", annotations[i].getElementName(), annotations[i].getElementName());
                }
            }
        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }                                                                                                                                   
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
                try {
                    icu.makeConsistent(null);
                } catch (JavaModelException e) {
                    e.printStackTrace();
                }
                result.add(icu);
            }
        }
        return result;
    }
    

    private String msgSuccessGenerateMethods(List<String> operations) {
        String result = "";
           result += format("메소드 생성에 성공하였습니다.\n");
           result += format("SVO = %s\n", getName(svo));
           result += format("DVO = %s\n ", getName(dvo));
           result += format("\n생성 결과:\n");
           for (String operation : operations) {
               result += format("\t%s\n", operation);
           }
        return result;
    }

    private String msgCannotFindSVODVO(List<ICompilationUnit> units) {
        String msg = "";
        msg += format("SVO, DVO를 찾지 못했습니다!\n");
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
        msg += format("SVO에 생성할 메소드가 모두 존재합니다! (toDVO, fromDVO)\n");
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
