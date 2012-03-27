// $Id: ConstraintViolationException.java,v 1.9 2004/11/18 08:42:43 grove Exp $

package net.ontopia.infoset.core;

import net.ontopia.utils.SDShareRuntimeException;;

/**
 * PUBLIC: Thrown when an object model constraint is violated.</p>
 *
 * Extends OntopiaRuntimeException but does not provide additional
 * methods; this is because the purpose is just to provide a different
 * exception, to allow API users to handle them differently.</p>
 */
public class ConstraintViolationException extends SDShareRuntimeException {

  public ConstraintViolationException(Throwable cause) {
    super(cause);
  }

  public ConstraintViolationException(String message) {
    super(message);
  }

  public ConstraintViolationException(String message, Throwable cause) {
    super(message, cause);
  }

}