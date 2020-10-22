use j4rs::prelude::*;
use j4rs_derive::*;
use raster::{filter, BlurMode, BlendMode, PositionMode};

#[call_from_java("bindings.RustDefs.exampleMethod")]
fn example_method(str_instance: Instance) -> Result<Instance, &'static str> {
    Err("Frigg")
}

#[call_from_java("bindings.RustDefs.blurImage")]
pub fn blur_image(path_str: Instance, passes: Instance) {
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

#[call_from_java("bindings.RustDefs.blendImages")]
pub fn blend_images(path1: Instance, path2: Instance) {
    match Jvm::attach_thread() {
        Ok(jvm) => {
            if let (Ok(path1_s), Ok(path2_s)) = (jvm.to_rust::<String>(path1), jvm.to_rust::<String>(path2)) {
                blend_image_impl(path1_s.as_str(), path2_s.as_str());
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

pub fn blend_image_impl(path1: &str, path2: &str) {
    let first_image = raster::open(path1).unwrap();
    let second_image = raster::open(path2).unwrap();

    match raster::editor::blend(&first_image, &second_image, BlendMode::Normal, 1.0, PositionMode::Center, 0, 0) {
        Ok(image) => {
            match raster::save(&image, "blend_output.png") {
                Ok(()) => println!("Saved image: blend_output.png"),
                Err(e) => eprintln!("Couldn\'t save image: {:?}", e)
            }
        },

        Err(e) => {
            eprintln!("Couldn\'t blend images:\n{:?}", e)
        }
    }

}
