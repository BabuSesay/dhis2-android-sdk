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

import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.parser.expression.item.DimItemDataElementAndOperand;
import org.hisp.dhis.android.core.parser.expression.item.ItemDays;
import org.hisp.dhis.android.core.validation.MissingValueStrategy;
import org.hisp.dhis.antlr.Parser;

import java.util.HashMap;
import java.util.Map;

import static org.hisp.dhis.antlr.AntlrParserUtils.COMMON_EXPRESSION_FUNCTIONS;
import static org.hisp.dhis.parser.expression.antlr.ExpressionParser.DAYS;
import static org.hisp.dhis.parser.expression.antlr.ExpressionParser.HASH_BRACE;

public class ExpressionService {

    private final Map<Integer, ExprItem> VALIDATION_RULE_EXPRESSION_ITEMS;

    public ExpressionService() {
        this.VALIDATION_RULE_EXPRESSION_ITEMS = getExpressionItems();
    }

    private Map<Integer, ExprItem> getExpressionItems() {
        Map<Integer, ExprItem> expressionItems = new HashMap<>();
        expressionItems.put(HASH_BRACE, new DimItemDataElementAndOperand());
        //expressionItems.put(OUG_BRACE, new ItemOrgUnitGroup());
        expressionItems.put(DAYS, new ItemDays());
        return expressionItems;
    }

    public Double getExpressionValue(String expression,
                                     Map<DataElementOperand, Double> valueMap, Map<String, Constant> constantMap,
                                     Map<String, Integer> orgUnitCountMap, Integer days,
                                     MissingValueStrategy missingValueStrategy ) {

        if (expression == null) {
            return null;
        }

        Map<String, Double> keyValueMap = new HashMap<>();
        for (Map.Entry<DataElementOperand, Double> entry : valueMap.entrySet()) {
            // TODO create key
            keyValueMap.put(entry.getKey().dataElement().uid(), entry.getValue());
        }

        CommonExpressionVisitor visitor = CommonExpressionVisitor.newBuilder()
                .withFunctionMap(COMMON_EXPRESSION_FUNCTIONS)
                .withItemMap(VALIDATION_RULE_EXPRESSION_ITEMS)
                .withExprItemMethod(ExprItem::evaluate)
                .validateCommonProperties();

        visitor.setItemValueMap(keyValueMap);

        if ( days != null )
        {
            visitor.setDays( Double.valueOf( days ) );
        }

        Object object = Parser.visit(expression, visitor);

        return (Double) object;
    }
}