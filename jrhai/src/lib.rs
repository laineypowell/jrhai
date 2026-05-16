use rhai::{Engine, EvalAltResult, Module, ModuleResolver, Position, Scope};
use std::ffi::{c_char, CStr, CString};
use std::rc::Rc;

type JavaModuleResolver = extern "C" fn(*mut c_char) -> *mut c_char;

struct JrhaiModuleResolver {
    resolver: JavaModuleResolver,
}

impl ModuleResolver for JrhaiModuleResolver {
    fn resolve(&self, engine: &Engine, source: Option<&str>, path: &str, pos: Position) -> Result<Rc<Module>, Box<EvalAltResult>> {
        unsafe  {
            let bytes = CString::new(path).expect("").into_raw();

            let script_ptr = (self.resolver)(bytes);

            let c_str = CStr::from_ptr(script_ptr);

            let script = c_str.to_str().expect("");

            let ast = engine.compile(script)?;

            let module = Module::eval_ast_as_new(Scope::new(), &ast, &engine)?;

            Ok(Rc::new(module))
        }
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn create_engine() -> *mut Engine {
    Box::into_raw(Box::new(Engine::new()))
}

#[unsafe(no_mangle)]
pub extern "C" fn destroy_engine(engine: *mut Engine) {
    unsafe { drop(Box::from_raw(engine)); }
}

#[unsafe(no_mangle)]
pub extern "C" fn engine_run(engine: *mut Engine, script: *const c_char) {
    unsafe {
        let c_str = CStr::from_ptr(script);

        let run_script = c_str.to_str().expect("");

        (*engine).run(run_script).expect("");
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn engine_set_module_resolver(engine: *mut Engine, resolver: JavaModuleResolver) {
    unsafe {
        (*engine).set_module_resolver(JrhaiModuleResolver { resolver });
    }
}