/*
 * Copyright (c) 2015 Riccardo Balbo < riccardo @ forkforge . net >
 * <p>
 * This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held
 * liable for any damages arising from the use of this software.
 * <p>
 * Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter
 * it and redistribute it freely, subject to the following restrictions:
 * <p>
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software.
 * If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not
 * required.
 * <p>
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original
 * software.
 * <p>
 * 3. This notice may not be removed or altered from any source distribution.
 */
package com.github.micheljung.transparentwebview;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;


/**
 * This patch aims to make the WebView capable to correctly render transparent pages.
 *
 * @author Riccardo Balbo
 */
public class TransparentWebViewPatch implements ClassFileTransformer {

  private final ClassPool CLASS_POOL = ClassPool.getDefault();

  public static void premain(String agentArguments, Instrumentation instrumentation) {
    instrumentation.addTransformer(new TransparentWebViewPatch());
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> class_being_redefined, ProtectionDomain protection_domain, byte[] byteCode) {
    if (className.equals("com/sun/webkit/WebPage")) {
      System.out.println("> Patching " + className + " ...");
      try {
        CtClass ctClass = CLASS_POOL.makeClass(new ByteArrayInputStream(byteCode));

        // First thing to do is edit the fireLoadEvent in order to force
        // the page to set its own
        // background to transparent black (0x00000000 argb) each time
        // it changes
        CtMethod fireLoadEventMethod = ctClass.getDeclaredMethod("fireLoadEvent");
        fireLoadEventMethod.insertBefore("{\n" + "    "
            + "setBackgroundColor(0);\n"
            + "}");

        // Then we replace the scroll method body in order to force the
        // repaint of the entire frame
        // when the page is scrolled
        CtMethod scrollMethod = ctClass.getDeclaredMethod("scroll");
        scrollMethod.setBody(
            "{\n" + "   "
                + "addDirtyRect(new com.sun.webkit.graphics.WCRectangle(0f,0f,(float)width,(float)height));\n"
                + "}"
        );
        byteCode = ctClass.toBytecode();
        ctClass.detach();
      } catch (Exception e) {
        System.out.println("/!\\ " + className + " patching failed :(");
        e.printStackTrace();
        return byteCode;
      }
      System.out.println("> " + className + " patching succeeded!");
    } else if ("com/sun/javafx/webkit/prism/WCGraphicsPrismContext".equals(className)) {
      System.out.println("> Patching " + className + " ...");
      try {
        CtClass ctClass = CLASS_POOL.makeClass(new ByteArrayInputStream(byteCode));

        // Then, we edit the the WCGraphicsPrismContext.setClip method
        // in order to call clearRect over the area of the clip.
        CtClass signature[] = new CtClass[]{CLASS_POOL.get("com.sun.webkit.graphics.WCRectangle")};
        CtMethod setClipMethod = ctClass.getDeclaredMethod("setClip", signature);
        setClipMethod.insertBefore(
            "{" + "  "
                + " $0.clearRect($1.getX(),$1.getY(),$1.getWidth(),$1.getHeight());"
                + "}");
        byteCode = ctClass.toBytecode();
        ctClass.detach();
      } catch (Exception e) {
        System.out.println("/!\\ " + className + " patching failed :(");
        e.printStackTrace();
        return byteCode;
      }
      System.out.println("> " + className + " patching succeeded!");
    }

    return byteCode;
  }
}
