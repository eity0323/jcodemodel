/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.helger.jcodemodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Variables and fields.
 */
public class JVar extends AbstractJExpressionImpl implements IJDeclaration, IJAssignmentTarget, IJAnnotatable
{
  /**
   * Modifiers.
   */
  private final JMods mods;

  /**
   * JType of the variable
   */
  private AbstractJType type;

  /**
   * Name of the variable
   */
  private String name;

  /**
   * Initialization of the variable in its declaration
   */
  private IJExpression init;

  /**
   * Annotations on this variable. Lazily created.
   */
  private List <JAnnotationUse> annotations;

  /**
   * JVar constructor
   * 
   * @param type
   *        Datatype of this variable
   * @param name
   *        Name of this variable
   * @param init
   *        Value to initialize this variable to
   */
  protected JVar (@Nonnull final JMods mods,
                  @Nonnull final AbstractJType type,
                  @Nonnull final String name,
                  @Nullable final IJExpression init)
  {
    if (!JJavaName.isJavaIdentifier (name))
      throw new IllegalArgumentException ("Illegal variable name '" + name + "'");
    this.mods = mods;
    this.type = type;
    this.name = name;
    this.init = init;
  }

  /**
   * Initialize this variable
   * 
   * @param init
   *        JExpression to be used to initialize this field
   */
  @Nonnull
  public JVar init (@Nullable final IJExpression init)
  {
    this.init = init;
    return this;
  }

  /**
   * Get the name of this variable
   * 
   * @return Name of the variable
   */
  @Nonnull
  public String name ()
  {
    return name;
  }

  /**
   * Changes the name of this variable.
   */
  public void name (@Nonnull final String name)
  {
    if (!JJavaName.isJavaIdentifier (name))
      throw new IllegalArgumentException ("Illegal variable name '" + name + "'");
    this.name = name;
  }

  /**
   * Return the type of this variable.
   * 
   * @return always non-null.
   */
  public AbstractJType type ()
  {
    return type;
  }

  /**
   * @return the current modifiers of this method. Always return non-null valid
   *         object.
   */
  @Nonnull
  public JMods mods ()
  {
    return mods;
  }

  /**
   * Sets the type of this variable.
   * 
   * @param newType
   *        must not be null.
   * @return the old type value. always non-null.
   */
  @Nonnull
  public AbstractJType type (@Nonnull final AbstractJType newType)
  {
    if (newType == null)
      throw new IllegalArgumentException ();
    final AbstractJType r = type;
    type = newType;
    return r;
  }

  /**
   * Adds an annotation to this variable.
   * 
   * @param clazz
   *        The annotation class to annotate the field with
   */
  @Nonnull
  public JAnnotationUse annotate (@Nonnull final AbstractJClass clazz)
  {
    if (annotations == null)
      annotations = new ArrayList <JAnnotationUse> ();
    final JAnnotationUse a = new JAnnotationUse (clazz);
    annotations.add (a);
    return a;
  }

  /**
   * Adds an annotation to this variable.
   * 
   * @param clazz
   *        The annotation class to annotate the field with
   */
  @Nonnull
  public JAnnotationUse annotate (@Nonnull final Class <? extends Annotation> clazz)
  {
    return annotate (type.owner ().ref (clazz));
  }

  @Nonnull
  public <W extends IJAnnotationWriter <?>> W annotate2 (@Nonnull final Class <W> clazz)
  {
    return TypedAnnotationWriter.create (clazz, this);
  }

  @Nonnull
  public Collection <JAnnotationUse> annotations ()
  {
    if (annotations == null)
      annotations = new ArrayList <JAnnotationUse> ();
    return Collections.unmodifiableList (annotations);
  }

  protected boolean isAnnotated ()
  {
    return annotations != null;
  }

  public void bind (@Nonnull final JFormatter f)
  {
    if (annotations != null)
      for (final JAnnotationUse annotation : annotations)
        f.generable (annotation).newline ();
    f.generable (mods).generable (type).id (name);
    if (init != null)
      f.print ('=').generable (init);
  }

  public void declare (@Nonnull final JFormatter f)
  {
    f.var (this).print (';').newline ();
  }

  public void generate (@Nonnull final JFormatter f)
  {
    f.id (name);
  }

  @Nonnull
  public IJExpressionStatement assign (@Nonnull final IJExpression rhs)
  {
    return JExpr.assign (this, rhs);
  }

  @Nonnull
  public IJExpressionStatement assignPlus (@Nonnull final IJExpression rhs)
  {
    return JExpr.assignPlus (this, rhs);
  }

  @Nonnull
  public IJExpressionStatement assignMinus (@Nonnull final IJExpression rhs)
  {
    return JExpr.assignMinus (this, rhs);
  }

  @Nonnull
  public IJExpressionStatement assignTimes (@Nonnull final IJExpression rhs)
  {
    return JExpr.assignTimes (this, rhs);
  }

  @Nonnull
  public IJExpressionStatement assignDivide (@Nonnull final IJExpression rhs)
  {
    return JExpr.assignDivide (this, rhs);
  }
}
