/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.parser.expression;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.Validate;
import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.antlr.AntlrExpressionVisitor;
import org.hisp.dhis.antlr.ParserExceptionWithoutContext;
import org.hisp.dhis.parser.expression.antlr.ExpressionParser.ExprContext;

import java.util.HashMap;
import java.util.Map;

import static org.hisp.dhis.android.core.parser.expression.ParserUtils.DOUBLE_VALUE_IF_NULL;

/**
 * Common traversal of the ANTLR4 org.hisp.dhis.rules.parser.expression parse tree using the
 * visitor pattern.
 *
 * @author Jim Grace
 */
public class CommonExpressionVisitor
        extends AntlrExpressionVisitor
{
    /**
     * Map of ExprItem instances to call for each expression item
     */
    private Map<Integer, ExpressionItem> itemMap;

    /**
     * Method to call within the ExprItem instance
     */
    private ExpressionItemMethod itemMethod;

    /**
     * By default, replace nulls with 0 or ''.
     */
    private boolean replaceNulls = true;

    /**
     * Constants to use in evaluating an expression.
     */
    private Map<String, Constant> constantMap = new HashMap<>();

    /**
     * Organisation unit group counts to use in evaluating an expression.
     */
    Map<String, Integer> orgUnitCountMap = new HashMap<>();

    /**
     * Count of days in period to use in evaluating an expression.
     */
    private Double days = null;

    /**
     * Values to use for variables in evaluating an org.hisp.dhis.rules.parser.expression.
     */
    private Map<String, Double> itemValueMap = new HashMap<>();

    /**
     * Count of dimension items found.
     */
    private int itemsFound = 0;

    /**
     * Count of dimension item values found.
     */
    private int itemValuesFound = 0;

    /**
     * Default value for data type double.
     */
    public static final double DEFAULT_DOUBLE_VALUE = 1d;


    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    protected CommonExpressionVisitor()
    {
    }

    /**
     * Creates a new Builder for CommonExpressionVisitor.
     *
     * @return a Builder for CommonExpressionVisitor.
     */
    public static Builder newBuilder()
    {
        return new CommonExpressionVisitor.Builder();
    }

    // -------------------------------------------------------------------------
    // Visitor methods
    // -------------------------------------------------------------------------

    @Override
    public Object visitExpr( ExprContext ctx )
    {
        if ( ctx.it != null )
        {
            ExpressionItem item = itemMap.get( ctx.it.getType() );

            if ( item == null )
            {
                throw new ParserExceptionWithoutContext(
                        "Item " + ctx.it.getText() + " not supported for this type of expression" );
            }

            return itemMethod.apply( item, ctx, this );
        }

        if ( ctx.expr().size() > 0 ) // If there's an expr, visit the expr
        {
            return visit( ctx.expr( 0 ) );
        }

        return visit( ctx.getChild( 0 ) ); // All others: visit first child.
    }

    // -------------------------------------------------------------------------
    // Logic for expression items
    // -------------------------------------------------------------------------

    /**
     * Visits a context while allowing null values (not replacing them
     * with 0 or ''), even if we would otherwise be replacing them.
     *
     * @param ctx any context
     * @return the value while allowing nulls
     */
    public Object visitAllowingNulls( ParserRuleContext ctx )
    {
        boolean savedReplaceNulls = replaceNulls;

        replaceNulls = false;

        Object result = visit( ctx );

        replaceNulls = savedReplaceNulls;

        return result;
    }

    /**
     * Handles nulls and missing values.
     * <p/>
     * If we should replace nulls with the default value, then do so, and
     * remember how many items found, and how many of them had values, for
     * subsequent MissingValueStrategy analysis.
     * <p/>
     * If we should not replace nulls with the default value, then don't,
     * as this is likely for some function that is testing for nulls, and
     * a missing value should not count towards the MissingValueStrategy.
     *
     * @param value the (possibly null) value
     * @return the value we should return.
     */
    public Object handleNulls( Object value )
    {
        if ( replaceNulls )
        {
            itemsFound++;

            if ( value == null )
            {
                return DOUBLE_VALUE_IF_NULL;
            }
            else
            {
                itemValuesFound++;
            }
        }

        return value;
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Map<String, Constant> getConstantMap()
    {
        return constantMap;
    }

    public boolean getReplaceNulls()
    {
        return replaceNulls;
    }

    public void setReplaceNulls( boolean replaceNulls )
    {
        this.replaceNulls = replaceNulls;
    }

    public Map<String, Integer> getOrgUnitCountMap()
    {
        return orgUnitCountMap;
    }

    public void setOrgUnitCountMap( Map<String, Integer> orgUnitCountMap )
    {
        this.orgUnitCountMap = orgUnitCountMap;
    }

    public Map<String, Double> getItemValueMap()
    {
        return itemValueMap;
    }

    public void setItemValueMap( Map<String, Double> itemValueMap )
    {
        this.itemValueMap = itemValueMap;
    }

    public Double getDays() {
        return this.days;
    }

    public void setDays(Double days) {
        this.days = days;
    }

    public int getItemsFound()
    {
        return itemsFound;
    }

    public int getItemValuesFound()
    {
        return itemValuesFound;
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Builder for {@link CommonExpressionVisitor} instances.
     */
    public static class Builder {
        private CommonExpressionVisitor visitor;

        protected Builder()
        {
            this.visitor = new CommonExpressionVisitor();
        }

        public Builder withItemMap( Map<Integer, ExpressionItem> itemMap )
        {
            this.visitor.itemMap = itemMap;
            return this;
        }

        public Builder withItemMethod( ExpressionItemMethod itemMethod )
        {
            this.visitor.itemMethod = itemMethod;
            return this;
        }

        public Builder withConstantMap( Map<String, Constant> constantMap )
        {
            this.visitor.constantMap = constantMap;
            return this;
        }

        private CommonExpressionVisitor validateCommonProperties() {
            Validate.notNull( this.visitor.constantMap, "Missing required property 'constantMap'" );
            Validate.notNull( this.visitor.itemMap, "Missing required property 'itemMap'" );
            Validate.notNull( this.visitor.itemMethod, "Missing required property 'itemMethod'" );
            return visitor;
        }

        public CommonExpressionVisitor buildForExpressions()
        {
            return validateCommonProperties();
        }
    }
}
