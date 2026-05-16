use rhai::Engine;
use std::ffi::{c_char, CStr};

#[unsafe(no_mangle)]
pub extern "C" fn create_engine() -> *mut Engine {
    Box::into_raw(Box::new(Engine::new()))
}

#[unsafe(no_mangle)]
pub extern "C" fn destroy_engine(engine: *mut Engine) {
    unsafe {
        drop(Box::from_raw(engine));
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn engine_run(ptr: *mut Engine, script: *const c_char) {
    unsafe {
        let c_str = CStr::from_ptr(script);
        
        let result = c_str.to_str().expect("");

        (*ptr).run(result).expect("");
    }
}