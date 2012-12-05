/*
 * Copyright 2007 Robin Helgelin
 * Copyright 2008 Jonathan Barker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nu.localhost.tapestry5.springsecurity.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * Render it's body depending whether the user is in a specific role or not.
 * 
 * @author Jonathan Barker
 * @author Robin Helgelin
 * @author Tapestry Project (doc comments)
 */
public class IfRole {

    /** 
     * If the logged in user matches this role, then the body of the IfRole component is rendered. If false, the body is
     * omitted.  This is retained for backward compatibility, and corresponds to a single entry in ifAnyGranted
     */
    @Parameter(required = false, defaultPrefix = "literal", principal=true)
    private String role;

    /**
     * A comma-separated list of roles is supplied to one or more of the
     * following parameters. If none are supplied, the default behavior is to
     * permit access. Behavior should be self-explanatory.
     */
    @Parameter(required = false, defaultPrefix = "literal")
    private String ifAllGranted;

    @Parameter(required = false, defaultPrefix = "literal")
    private String ifAnyGranted;

    @Parameter(required = false, defaultPrefix = "literal")
    private String ifNotGranted;

    /**
     * Optional parameter to invert the test. If true, then the body is rendered when the test
     * parameter is false (not true).
     */
    @Parameter
    private boolean negate;

    /**
     * An alternate {@link Block} to render if the test parameter is false. The default, null, means
     * render nothing in that situation.
     */
    @Parameter(name = "else")
    private Block elseBlock;

    private boolean test;

    @SuppressWarnings("unchecked")
	private Collection<GrantedAuthority> getPrincipalAuthorities() {
        Authentication currentUser = null;
        currentUser = SecurityContextHolder.getContext().getAuthentication();

        if (null == currentUser) {
            return Collections.emptyList();
        }

        if ((null == currentUser.getAuthorities()) || (currentUser.getAuthorities().size() < 1)) {
            return Collections.emptyList();
        }

        return (Collection<GrantedAuthority>) currentUser.getAuthorities();
    }

    private Collection<GrantedAuthority> authoritiesToRoles(Collection<GrantedAuthority> c) {
    	Collection<GrantedAuthority> target = new ArrayList<GrantedAuthority>();

        for (final GrantedAuthority authority : c) {
            
            if (null == authority.getAuthority()) {
                throw new IllegalArgumentException(
                    "Cannot process GrantedAuthority objects which return null from getAuthority() - attempting to process "
                    + authority.toString());
            }

            target.add(authority);
        }

        return target;
    }

    private Collection<GrantedAuthority> parseAuthoritiesString(String authorizationsString) {
        final Collection<GrantedAuthority> requiredAuthorities = new ArrayList<GrantedAuthority>();
        final String[] authorities = StringUtils.commaDelimitedListToStringArray(authorizationsString);

        for (int i = 0; i < authorities.length; i++) {
            String authority = authorities[i];

            // Remove the role's whitespace characters without depending on JDK 1.4+
            // Includes space, tab, new line, carriage return and form feed.
            String role = StringUtils.replace(authority, " ", "");
            role = StringUtils.replace(role, "\t", "");
            role = StringUtils.replace(role, "\r", "");
            role = StringUtils.replace(role, "\n", "");
            role = StringUtils.replace(role, "\f", "");

            requiredAuthorities.add(new SimpleGrantedAuthority(role));
        }
        return requiredAuthorities;
    }

    /**
     * Find the common authorities between the current authentication's {@link
     * GrantedAuthority} and the ones that have been specified in the tag's
     * ifAny, ifNot or ifAllGranted attributes.
     * 
     * <p>
     * We need to manually iterate over both collections, because the granted
     * authorities might not implement {@link Object#equals(Object)} and
     * {@link Object#hashCode()} in the same way as {@link
     * GrantedAuthority}, thereby invalidating {@link
     * Collection#retainAll(java.util.Collection)} results.
     * </p>
     * 
     * <p>
     * <strong>CAVEAT</strong>: This method <strong>will not</strong> work if
     * the granted authorities returns a <code>null</code> string as the
     * return value of {@link
     * org.springframework.security.GrantedAuthority#getAuthority()}.
     * </p>
     * 
     * <p>
     * Reported by rawdave, on Fri Feb 04, 2005 2:11 pm in the Spring Security
     * System for Spring forums.
     * </p>
     * 
     * @param granted
     *            The authorities granted by the authentication. May be any
     *            implementation of {@link GrantedAuthority} that does
     *            <strong>not</strong> return <code>null</code> from {@link
     *            org.springframework.security.GrantedAuthority#getAuthority()}.
     * @param required
     *            A {@link Set} of {@link GrantedAuthority}s that have been
     *            built using ifAny, ifAll or ifNotGranted.
     * 
     * @return A set containing only the common authorities between <var>granted</var>
     *         and <var>required</var>.
     * 
     */
    
    private Collection<GrantedAuthority> retainAll(final Collection<GrantedAuthority> granted, final Collection<GrantedAuthority> required) {
    	Collection<GrantedAuthority> grantedRoles = authoritiesToRoles(granted);
    	Collection<GrantedAuthority> requiredRoles = authoritiesToRoles(required);
    	ArrayList<GrantedAuthority> intersection = new ArrayList<GrantedAuthority>();
    	for (GrantedAuthority requiredAuthory : requiredRoles) {
			if(grantedRoles.contains(requiredAuthory)){
				intersection.add(requiredAuthory);
			}else{
			}
		}
        

        return intersection;
    }


    /**
     * @return false as the default.  Returns true if all non-null role expressions are 
     * satisfied.  Typically, only one will be used, but if more than one are used, then 
     * the conditions are effectively AND'd 
     */
    private boolean checkPermission() {
    	boolean returnValue = true;
        if (((null == ifAllGranted) || "".equals(ifAllGranted))
         && ((null == ifAnyGranted) || "".equals(ifAnyGranted))
         && ((null == role) || "".equals(role))
         && ((null == ifNotGranted) || "".equals(ifNotGranted))) {
            return false;
        }

        final Collection<GrantedAuthority> granted = getPrincipalAuthorities();

        if ((null != role) && !"".equals(role)) {
        	final Collection<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(role));
            if (grantedCopy.isEmpty()) {
                returnValue = false;
            }
        }

        if ((null != ifNotGranted) && !"".equals(ifNotGranted)) {
        	final Collection<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(ifNotGranted));
            if (!grantedCopy.isEmpty()) {
                returnValue = false;
            }
        }

        if ((null != ifAllGranted) && !"".equals(ifAllGranted)) {
            if (!granted.containsAll(parseAuthoritiesString(ifAllGranted))) {
                returnValue = false;
            } 
        }

        if ((null != ifAnyGranted) && !"".equals(ifAnyGranted)) {
        	final Collection<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(ifAnyGranted));

            if (grantedCopy.isEmpty()) {
                returnValue = false;
            }
        }

        return returnValue;
    }


    void setupRender() {
        test = checkPermission();
    }

    /**
     * Returns null if the test method returns true, which allows normal
     * rendering (of the body). If the test parameter is false, returns the else
     * parameter (this may also be null).
     */
    Object beginRender() {
        return test != negate ? null : elseBlock;
    }

    /**
     * If the test method returns true, then the body is rendered, otherwise not. The component does
     * not have a template or do any other rendering besides its body.
     */
    boolean beforeRenderBody() {
        return test != negate;
    }
}
