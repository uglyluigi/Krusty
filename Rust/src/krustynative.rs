use j4rs::prelude::*;
use j4rs_derive::*;
use j4rs::InvocationArg;

use raster::{filter, BlurMode, BlendMode, PositionMode};
use std::convert::TryFrom;

#[call_from_java("bindings.RustDefs.exampleMethod")]
fn example_method(str_instance: Instance) -> Result<Instance, &'static str> {
    Err("Frigg")
}

#[call_from_java("bindings.RustDefs.blurImage")]
pub fn blur_image(path_str: Instance, passes: Instance) -> Result<Instance, String> {
    match Jvm::attach_thread() {
        Ok(jvm) => {
            if let (Ok(path), Ok(num_passes)) = (jvm.to_rust::<String>(path_str), jvm.to_rust::<i32>(passes)) {
                println!("Running blur_image_impl");
                

                return match blur_image_impl(path.as_str(), num_passes) {
                    Some(path) => {
                        let inv_arg = InvocationArg::try_from(path).map_err(|error| format!("{}", error)).unwrap();
                        Instance::try_from(inv_arg).map_err(|error| format!("{}", error))
                    },
                    None => Err("Error during image blurring".to_string()),
                }
            } else {
                eprintln!("Unable to create Rust representations");
                Err("Oh no!!!".to_string())
            }
        },

        Err(_) => Err("Oh no!!!".to_string())
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

pub fn blur_image_impl(path: &str, passes: i32) -> Option<String> {
    match raster::open(path) {
        Ok(mut image) => {
            for i in 0..passes {
                println!("Running blur pass {}", i);
                match filter::blur(&mut image, BlurMode::Gaussian) {
                    Ok(()) => println!("Finished blur pass {}", i),
                    Err(err) => eprintln!("Couldn\'t apply blur:\n{:?}", err)
                }
            }

            let old_path = path.split(".").collect::<Vec<&str>>();
            let new_path = format!("{}_out.{}", old_path[0], old_path[1]);
                            
            match raster::save(&image, new_path.as_str()) {
                Ok(()) => println!("saved {}", new_path),
                Err(s) => eprintln!("couldn\'t save {}:\n{:?}", new_path, s)
            }

            return Some(new_path);
        },

        Err(err) => eprintln!("Raster-rs couldn\'t open file:\n{:?}", err),
    }

    None
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

#[call_from_java("bindings.RustDefs.print")]
pub fn print(instance: Instance) {
    match Jvm::attach_thread() {
        Ok(jvm) => {
            if let Ok(thing) = jvm.to_rust::<String>(instance) {
                println!("{}", thing)
            }
        },

        Err(err) => eprintln!("Couldn\'t attach JVM thread:\n{}", err)
    }
}