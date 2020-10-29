use j4rs::prelude::*;
use j4rs_derive::*;
use j4rs::InvocationArg;

use raster::{filter, BlurMode, BlendMode, PositionMode, Color, Image};
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
                        match get_string_instance(&jvm, path) {
                            Some(instance) => Ok(instance),
                            None => Err("Error creating string instance".to_string())
                        }
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

            let new_path = get_output_path(path);
                            
            match raster::save(&image, new_path.as_str()) {
                Ok(()) => println!("saved {}", new_path),
                Err(s) => eprintln!("couldn\'t save {}:\n{:?}", new_path, s)
            }


            return Some(new_path.to_string());
        },

        Err(err) => eprintln!("Raster-rs couldn\'t open file:\n{:?}", err),
    }

    None
}

pub fn blend_image_impl(path1: &str, path2: &str) -> String {
    let first_image = raster::open(path1).unwrap();
    let second_image = raster::open(path2).unwrap();

    match raster::editor::blend(&first_image, &second_image, BlendMode::Normal, 1.0, PositionMode::Center, 0, 0) {
        Ok(mut image) => {
            match raster::save(&image, "blend_output.png") {
                Ok(()) => println!("Saved image: blend_output.png"),
                Err(e) => eprintln!("Couldn\'t save image: {:?}", e)
            }

            let output_path = get_output_path(path1);
            save(&mut image, output_path.as_str());
            output_path.to_string()
        },

        Err(e) => {
            eprintln!("Couldn\'t blend images:\n{:?}", e);
            String::new()
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

#[call_from_java("bindings.RustDefs.rotateImage")]
pub fn rotate_image(path: Instance, degree: Instance, r: Instance, g: Instance, b: Instance) -> Result<Instance, String> {
    return match Jvm::attach_thread() {
        Ok(jvm) => {
            if let 
            (Ok(path), 
            Ok(degree), 
            Ok(r), 
            Ok(g), 
            Ok(b)) 
            = 
            (jvm.to_rust::<String>(path), 
            jvm.to_rust::<i32>(degree),
            jvm.to_rust::<i32>(r),
            jvm.to_rust::<i32>(g),
            jvm.to_rust::<i32>(b),
        ) {
                println!("Rotating image: {}", path);

                return match rotate_image_impl(path.as_str(), degree, r as u8, g as u8, b as u8) {
                    Some(path) => {
                        match get_string_instance(&jvm, path) {
                            Some(instance) => Ok(instance),
                            None => Err("Couldn\'t create string instance".to_string())
                        }
                    },

                    None => Err("Couldn\'t rotate image".to_string())
                }
            } else {
                Err("Failed to create rust representations".to_string())
            }
        },

        Err(err) => Err(format!("{:?}", err))
    }
}

pub fn rotate_image_impl(path: &str, degree: i32, r: u8, g: u8, b: u8) -> Option<String> {
    let mut image = raster::open(path).unwrap();

    match raster::transform::rotate(&mut image, degree, Color::rgb(r, g, b)) {
        Ok(()) => println!("Applied {} degree rotation to image", degree),
        Err(err) => {
            println!("Error when rotating image: \n{:?}", err);
            return None;
        }
    }

    let output_path = get_output_path(path);
    save(&mut image, output_path.as_str());
    Some(output_path.to_string())
}

fn save(image: &mut Image, path: &str) -> Result<(), ()> {
    match raster::save(image, path) {
        Ok(()) => {
            println!("Saved path: {}", path);
            Ok(())
        }
        Err(err) => {
            eprintln!("Error when saving image: {:?}", err);
            Err(())
        }
    }
}

fn get_output_path(path: &str) -> String {
    let old_path = path.split(".").collect::<Vec<&str>>();
    format!("{}_out.{}", old_path[0], old_path[1])
}

fn get_string_instance(jvm: &Jvm, s: String) -> Option<Instance> {
    let inv_arg = InvocationArg::try_from(s).map_err(|error| format!("{}", error)).unwrap();
    match Instance::try_from(inv_arg).map_err(|error| format!("{}", error)) {
        Ok(instance) => Some(instance),
        Err(err) => {
            eprintln!("Error creating string instance: {}", err);
            None
        }
    }
}