/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright 2013-2015 Philip Helger
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

import static com.helger.jcodemodel.util.EqualsUtils.isEqual;
import static com.helger.jcodemodel.util.HashCodeGenerator.getHashCode;

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
public class JVar extends AbstractJExpressionAssignmentTargetImpl implements IJDeclaration, IJAnnotatable
{
  /**
   * Modifiers.
   */
  private final JMods m_aMods;

  /**
   * JType of the variable
   */
  private AbstractJType m_aType;

  /**
   * Name of the variable
   */
  private String m_sName;

  /**
   * Initialization of the variable in its declaration
   */
  private IJExpression m_aInitExpr;

  /**
   * Annotations on this variable. Lazily created.
   */
  private List <JAnnotationUse> _annotations;

  /**
   * JVar constructor
   *
   * @param aType
   *        Datatype of this variable
   * @param sName
   *        Name of this variable
   * @param aInitExpr
   *        Value to initialize this variable to
   */
  public JVar (@Nonnull final JMods aMods,
               @Nonnull final AbstractJType aType,
               @Nonnull final String sName,
               @Nullable final IJExpression aInitExpr)
  {
    if (!JJavaName.isJavaIdentifier (sName))
      throw new IllegalArgumentException ("Illegal variable name '" + sName + "'");
    m_aMods = aMods;
    m_aType = aType;
    m_sName = sName;
    m_aInitExpr = aInitExpr;
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
    m_aInitExpr = init;
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
    return m_sName;
  }

  /**
   * Changes the name of this variable.
   */
  public void name (@Nonnull final String name)
  {
    if (!JJavaName.isJavaIdentifier (name))
      throw new IllegalArgumentException ("Illegal variable name '" + name + "'");
    m_sName = name;
  }

  /**
   * Return the type of this variable.
   *
   * @return always non-null.
   */
  public AbstractJType type ()
  {
    return m_aType;
  }

  /**
   * @return the current modifiers of this method. Always return non-null valid
   *         object.
   */
  @Nonnull
  public JMods mods ()
  {
    return m_aMods;
  }

  /**
   * Sets the type of this variable.
   *
   * @param aNewType
   *        must not be null.
   * @return the old type value. always non-null.
   */
  @Nonnull
  public AbstractJType type (@Nonnull final AbstractJType aNewType)
  {
    if (aNewType == null)
      throw new IllegalArgumentException ();
    final AbstractJType aOldType = m_aType;
    m_aType = aNewType;
    return aOldType;
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
    if (_annotations == null)
      _annotations = new ArrayList <JAnnotationUse> ();
    final JAnnotationUse a = new JAnnotationUse (clazz);
    _annotations.add (a);
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
    return annotate (m_aType.owner ().ref (clazz));
  }

  @Nonnull
  public <W extends IJAnnotationWriter <?>> W annotate2 (@Nonnull final Class <W> clazz)
  {
    return TypedAnnotationWriter.create (clazz, this);
  }

  @Nonnull
  public Collection <JAnnotationUse> annotations ()
  {
    if (_annotations == null)
      _annotations = new ArrayList <JAnnotationUse> ();
    return Collections.unmodifiableList (_annotations);
  }

  protected boolean isAnnotated ()
  {
    return _annotations != null;
  }

  public void bind (@Nonnull final JFormatter f)
  {
    if (_annotations != null)
      for (final JAnnotationUse annotation : _annotations)
        f.generable (annotation).newline ();
    f.generable (m_aMods).generable (m_aType).id (m_sName);
    if (m_aInitExpr != null)
      f.print ('=').generable (m_aInitExpr);
  }

  public void declare (@Nonnull final JFormatter f)
  {
    f.var (this).print (';').newline ();
  }

  public void generate (@Nonnull final JFormatter f)
  {
    f.id (m_sName);
  }

  @Override
  public boolean equals (Object o)
  {
    if (o == this)
      return true;
    if (!(o instanceof IJExpression))
      return false;
    o = ((IJExpression) o).unwrapped ();
    if (!(o instanceof JVar))
      return false;
    final JVar rhs = (JVar) o;
    return isEqual (m_sName, rhs.m_sName);
  }

  @Override
  public int hashCode ()
  {
    return getHashCode (this, m_sName);
  }

  @Override
  AbstractJType derivedType ()
  {
    return m_aType;
  }

  @Override
  String derivedName ()
  {
    return name ();
  }
}
