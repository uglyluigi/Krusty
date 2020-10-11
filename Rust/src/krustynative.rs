use j4rs::prelude::*;
use j4rs_derive::*;
use raster::{filter, BlurMode};

#[call_from_java("bindings.RustDefs.exampleMethod")]
fn example_method(str_instance: Instance) -> Result<Instance, &'static str> {
    Err("Frigg")
}

#[call_from_java("bindings.RustDefs.blurImage")]
pub fn blur_image(path_str: Instance, passes: Instance) {
    //Stuff from Java
    let jvm: Jvm = Jvm::attach_thread().unwrap();

    match Jvm::attach_thread() {
        Ok(jvm) => {
            if let (Ok(path), Ok(num_passes)) = (jvm.to_rust::<String>(path_str), jvm.to_rust::<i32>(passes)) {
                blur_image_impl(path.as_str(), num_passes);
            } else {
                eprintln!("Unable to create Rust representations");
            }
        },

        Err(err) => eprintln!("Couldn\'t attach JVM thread:\n{}", err)
    }
}

pub fn blur_image_impl(path: &str, passes: i32) {
    match raster::open(path) {
        Ok(mut image) => {
            for _ in 0..passes {
                filter::blur(&mut image, BlurMode::Gaussian).unwrap();
            }
        
            let new_path = "output.png";
        
            match raster::save(&image, new_path.clone()) {
                Ok(()) => println!("saved {}", new_path),
                Err(s) => { 
                    eprintln!("couldn\'t save {}:\n{:?}", new_path, s);
                    
                }
            }
        },

        Err(err) => eprintln!("Raster-rs couldn\'t open file:\n{:?}", err),
    }
}