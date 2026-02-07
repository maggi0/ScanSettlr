package com.scansettler.schema;

import com.scansettler.models.UsernameAmount;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetExpenseResponse
{
    private String name;
    private UsernameAmount lender;
    private List<UsernameAmount> borrowers;
}
