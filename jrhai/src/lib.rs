use rhai::{Engine, EvalAltResult, Module, ModuleResolver, Position, Scope};
use std::ffi::{c_char, CStr, CString};
use std::rc::Rc;
use std::str::Utf8Error;

type JavaModuleResolver = extern "C" fn(*const c_char) -> *const c_char;

struct JrhaiModuleResolver {
    resolver: JavaModuleResolver,
}

fn c_str(ptr: *const c_char) -> Result<String, Utf8Error> {
    let c_str = unsafe { CStr::from_ptr(ptr) };

    let str = c_str.to_str()?;

    Ok(str.to_owned())
}

impl ModuleResolver for JrhaiModuleResolver {
    fn resolve(&self, engine: &Engine, _source: Option<&str>, path: &str, _pos: Position) -> Result<Rc<Module>, Box<EvalAltResult>> {
        let c_string = CString::new(path).expect("").into_raw();

        let script_ptr = (self.resolver)(c_string);

        let script = c_str(script_ptr).expect("");

        let ast = engine.compile(script)?;

        let module = Module::eval_ast_as_new(Scope::new(), &ast, &engine)?;

        Ok(Rc::new(module))
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
        let str = c_str(script).expect("");

        (*engine).run(&*str).expect("");
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn engine_set_module_resolver(engine: *mut Engine, resolver: JavaModuleResolver) {
    unsafe {
        (*engine).set_module_resolver(JrhaiModuleResolver { resolver });
    }
}